package tasks

import extensions.AdmExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

class WaitAdsStartedTask extends DefaultTask {

    @Internal
    String START_CONDITION = "Application command 'server' was executed successfully."

    @Input
    AdmExtension adm

    @Input
    int admHash

    @TaskAction
    public execute() {
        def process = adm.process
        logger.info("Waiting for ADS start... ")

        BufferedReader reader =
                new BufferedReader(new InputStreamReader(process.getInputStream()));


        List<String> text = new ArrayList<>();
        String line;
        while (process.isAlive() && (line = reader.readLine()) != null) {
            logger.info(line)
            text.add(line)
            if (line.contains(START_CONDITION)) {
                break
            }
        }

        if (process.isAlive()) {
            logger.info("ADS started!")
        } else {
            logger.warn( "ADS failed to start!")
            text.stream().forEach(System.err::println)
            throw new RuntimeException("ADS failed to start, see output above")
        }
    }
}
