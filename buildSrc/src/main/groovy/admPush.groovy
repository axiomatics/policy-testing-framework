import groovy.json.JsonSlurper
import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.TaskAction

import java.nio.charset.Charset

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

    @TaskAction
    void run() {
        def accessToken = doOIDCLoginAndGetAccessToken()
        //println accessToken

       def body = makeBody()

        def postDomain = "${url}/namespaces/${namespace}/names/${domain}"
        def req = new URL(postDomain).openConnection()

        req.setRequestProperty("Authorization",  "Bearer ${accessToken}");
        req.setRequestMethod("POST")
        req.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        req.setDoOutput(true)
        req.getOutputStream().write(body.getBytes("UTF-8"))
        if (req.getResponseCode() != 201) {
            throw new RuntimeException("Push domain to ${postDomain} failed with ${req.responseCode}")
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
        def authClient = HttpClients.createDefault()
        org.apache.http.HttpResponse response = authClient.execute(post);

        //check auth response
        int responseCode = response.getStatusLine().getStatusCode()
        if (responseCode != 200) {
            throw new RuntimeException("Failed to perform OpenId Connect authentication to ASM-ADM at ${tokenUri}. Response code: ${responseCode}." +
                    " Response body ${EntityUtils.toString(response.getEntity())}")
        }
        new JsonSlurper().parseText(EntityUtils.toString(response.getEntity())).access_token
    }

}