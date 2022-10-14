import groovy.json.JsonSlurper
import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicNameValuePair
import org.apache.http.ssl.SSLContextBuilder
import org.apache.http.ssl.TrustStrategy
import org.apache.http.util.EntityUtils
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.TaskAction

import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.nio.charset.Charset
import java.security.cert.CertificateException
import java.security.cert.X509Certificate

class AdmPush extends org.gradle.api.DefaultTask implements Runnable {

    @org.gradle.api.tasks.InputFiles
    FileCollection domainFile

    @org.gradle.api.tasks.Input
    String client_secret
    @org.gradle.api.tasks.Input
    String namespace
    @org.gradle.api.tasks.Input
    String domain
    @org.gradle.api.tasks.Input
    String url
    @org.gradle.api.tasks.Input
    String tokenUri
    @org.gradle.api.tasks.Input
    String client_id

    @org.gradle.api.tasks.Internal
    String newDomainId

    @org.gradle.api.tasks.Input
    @org.gradle.api.tasks.Optional
    Boolean onlyPrintAccessToken = false


    @org.gradle.api.tasks.Input
    @org.gradle.api.tasks.Optional
    Boolean insecure = false



    @TaskAction
    void run() {
        def accessToken = doOIDCLoginAndGetAccessToken()
        if (onlyPrintAccessToken) {
            println "ASM access token obtained, no push made to remote server:\n${accessToken}"
            return
        }

        def body = makeBody()

        def postDomain = "${url}/namespaces/${namespace}/names/${domain}"
        URLConnection req = new URL(postDomain).openConnection()

        if (insecure && req instanceof HttpsURLConnection) {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, new TrustManager[]{new TrustAll()},
                    new java.security.SecureRandom());
            ((HttpsURLConnection) req).setSSLSocketFactory(sc.getSocketFactory())
        }

        req.setRequestProperty("Authorization",  "Bearer ${accessToken}");
        req.setRequestMethod("POST")
        req.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        req.setDoOutput(true)
        req.getOutputStream().write(body.getBytes("UTF-8"))
        if (req.getResponseCode() != 201) {
            throw new RuntimeException("Push domain to ${postDomain} failed with ${req.responseCode}, expected 201. Check your URL to ASM. Normally it is 'https://server/adm/api. Respone body: "+req.inputStream.text)
        }
        newDomainId = new JsonSlurper().parseText(req.inputStream.text).domainId
        println "Successfully posted domain to ${postDomain}. Server returned new domainId ${newDomainId}"
    }

    private makeBody() {
        com.fasterxml.jackson.dataformat.yaml.YAMLFactory
        def yamlString = domainFile.singleFile.text
        com.fasterxml.jackson.databind.ObjectMapper yamlReader =
                new com.fasterxml.jackson.databind.ObjectMapper(
                        new com.fasterxml.jackson.dataformat.yaml.YAMLFactory());
        Object obj = yamlReader.readValue(yamlString, Object.class);
        com.fasterxml.jackson.databind.ObjectMapper jsonWriter = new com.fasterxml.jackson.databind.ObjectMapper();
        jsonWriter.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    }
    private doOIDCLoginAndGetAccessToken() {
        //Create auth request
        List<NameValuePair> bodyElements = new ArrayList<>();
        bodyElements.add(new BasicNameValuePair("client_id", client_id));
        bodyElements.add(new BasicNameValuePair("client_secret", client_secret));
        bodyElements.add(new BasicNameValuePair("grant_type", "client_credentials"));
        bodyElements.add(new BasicNameValuePair("scope", "openid"));
        UrlEncodedFormEntity authBody = new UrlEncodedFormEntity(bodyElements, (Charset) null);
        HttpPost post = new HttpPost(tokenUri);
        post.setEntity(authBody);
        CloseableHttpClient authClient;
        if (insecure) {
            println("Insecure TLS connection to " + tokenUri)
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustStrategy() {
                @Override
                boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true
                }
            });
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build(), new HostnameVerifier() {
                @Override
                boolean verify(String hostname, SSLSession session) {
                    return true
                }
            });

            authClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

        } else {
            authClient = HttpClients.createDefault()
        }
        org.apache.http.HttpResponse response = authClient.execute(post);

        //check auth response
        int responseCode = response.getStatusLine().getStatusCode()
        if (responseCode != 200) {
            throw new RuntimeException("Failed to perform OpenId Connect authentication to ASM-ADM at ${tokenUri}. Response code: ${responseCode}." +
                    " Response body '${EntityUtils.toString(response.getEntity())}'")
        }
        new JsonSlurper().parseText(EntityUtils.toString(response.getEntity())).access_token
    }

    public static class TrustAll implements X509TrustManager {

        @Override
        void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            //trust all
        }

        @Override
        void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            //trust all
        }

        @Override
        X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0]
        }
    }
}