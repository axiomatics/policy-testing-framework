package tasks

import extensions.AdmExtension
import extensions.AlfaExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class StopAdmAdsTask extends  DefaultTask {

    @Input
    AdmExtension adm;
    @Input
    AlfaExtension config
    @Input
    int admHash

    @TaskAction
    stop() {
        logger.info("Stopping ADS ${adm.environment} on port ${adm.adsHttpPort}")
        if (adm.process == null) {
            logger.info("No process to stop")
            return
        }
        adm.process.waitForOrKill(2000)
        logger.info("ADS is stopped!")
        if (adm.process.isAlive()) {
            logger.info("Could not stop ADS ${adm.environment} on port ${adm.adsHttpPort}!")
        }
    }
}
