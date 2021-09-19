package propraganda.praktikum.logic.security.github;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import propraganda.praktikum.logic.util.JwtHelper;


@Component
@Slf4j
//@Profile("prod")
public class GithubLogin {

    public transient GitHub gitHub;

    @Value("${propraganda.github.appID}")
    private transient String appID;

    @Value("${propraganda.github.installationID}")
    private transient long installationID;

    //@Value("${propraganda.github.organisationName}")
    //private String organisationName;

    @Bean
    public void build() {
        try {
            log.info("Starting Github Connection...");

            String jwt = null;
            try {
                jwt = JwtHelper.createJWT("key.der", appID, 60_000);
            } catch (Exception exception) {
                log.error(exception.getMessage());
            }

            final  GitHub preAuth = new GitHubBuilder().withJwtToken(jwt).build();

            final GHAppInstallation appInstallation = preAuth.getApp().getInstallationById(installationID);
            final GHAppInstallationToken token = appInstallation.createToken().create();

            gitHub = new GitHubBuilder().withAppInstallationToken(token.getToken()).build();

            //GHOrganization organization = gitHub.getOrganization(organisationName);


            //GHUser user = gitHub.getUser("nino-salih");

            //repository.addCollaborators(user);


            log.info("Github Connection Done");
        }catch(Exception e) {
            log.error(e.getMessage());
        }
    }

    public GitHub getGitHub() {
        return gitHub;
    }
}
