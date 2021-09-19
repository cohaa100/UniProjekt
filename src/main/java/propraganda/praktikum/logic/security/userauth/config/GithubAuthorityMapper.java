package propraganda.praktikum.logic.security.userauth.config;

import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import propraganda.praktikum.logic.aggregate.benutzer.Benutzer;
import propraganda.praktikum.logic.aggregate.benutzer.BenutzerTyp;
import propraganda.praktikum.logic.services.benutzer.BenutzerService;
import propraganda.praktikum.logic.services.github.GithubService;

import java.io.IOException;
import java.util.*;

@Configuration
@Slf4j
public class GithubAuthorityMapper {


    @Autowired
    transient GithubService githubService;

    @Autowired
    transient BenutzerService benutzerService;

    @Bean
    public GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return (authorities) -> {
            final Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

            authorities.forEach(authority -> {
                if (OidcUserAuthority.class.isInstance(authority)) {

                    log.info("40");
                    // Map the claims found in idToken and/or userInfo
                    // to one or more GrantedAuthority's and add it to mappedAuthorities

                } else if (OAuth2UserAuthority.class.isInstance(authority)) {
                    final OAuth2UserAuthority oauth2UserAuthority = (OAuth2UserAuthority) authority;

                    final Map<String, Object> userAttributes = oauth2UserAuthority.getAttributes();

                    // Map the attributes found in userAttributes
                    // to one or more GrantedAuthority's and add it to mappedAuthorities

                    final int githubId = (int) userAttributes.get("id");

                    final Optional<Benutzer> benutzer = benutzerService.findBenutzerByGithubId(githubId);
                    if (benutzer.isPresent()) {
                        log.info("Die BenutzerId ist: {}", benutzer.toString());
                        log.info("Der Benutzer ist: {}", benutzer);
                        mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + benutzer.get().getBenutzerTyp().name()));
                    } else {
                        log.info("Else wird ausgefuehrt");
                        try {
                            final Optional<GHUser> ghUser = githubService.getGhUserInOrganisation(githubId);
                            if(ghUser.isPresent()) {
                                benutzerService.benutzerHinzufuegen(BenutzerTyp.STUDENT, (String) userAttributes.get("login"));
                                mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_STUDENT"));
                            } else {
                                mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_BESUCHER"));
                            }
                        } catch (IOException e) {
                            log.error(e.getMessage());
                        }

                    }

                }

                log.info(mappedAuthorities.toString());
            });

            return mappedAuthorities;
        };
    }
}
