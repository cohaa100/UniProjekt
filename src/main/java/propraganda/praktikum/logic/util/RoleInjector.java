package propraganda.praktikum.logic.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import propraganda.praktikum.logic.aggregate.benutzer.BenutzerTyp;
import propraganda.praktikum.logic.services.benutzer.BenutzerService;


@Component
@Slf4j
public class RoleInjector {

    @Autowired
    private transient BenutzerService benutzerService;

    @Value("${propraganda.roleconfig}")
    private transient String configPath;

    private final transient YmlConfig ymlConfig = new YmlConfig();


    @Bean
    void injectRoleBean(){

        try {
            final RoleConfig roleConfig = ymlConfig.getUserTypes(configPath);
            roleConfig.getOrganisator().parallelStream().forEach(
                    github -> {
                        benutzerService.benutzerHinzufuegen(BenutzerTyp.ORGANISATOR, github);
                        log.info("ORGANISATOR: {} erfolgreich hinzugefuegt", github.toString());
                    });

            roleConfig.getTutoren().parallelStream().forEach(
                    github -> {
                        benutzerService.benutzerHinzufuegen(BenutzerTyp.TUTOR, github);
                        log.info("TUTOR: {} erfolgreich hinzugefuegt", github.toString());
                    });

            //Changed to Exception due to NPE pmd
        } catch (Exception exception) {
               log.error(exception.toString());
        }
    }
}
