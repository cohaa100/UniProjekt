package propraganda.praktikum.logic.services.github;

import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import propraganda.praktikum.logic.security.github.GithubLogin;

import java.io.IOException;
import java.util.Optional;

@Component
@Slf4j
//@Profile("prod")
public class GithubService {

    @Autowired
    private transient GithubLogin githubLogin;

    @Value("${propraganda.github.organisationName}")
    private transient String organisation;


    public GHUser getGitHubUserByNameInOrga(final String ghName) throws IOException {
        final GHUser ghUser = githubLogin.getGitHub().getUser(ghName);
        if(ghUser == null || getGhUserInOrganisation(ghUser.getId()).isEmpty()){
            throw new UsernameNotFoundException("Der Benutzer " + ghName + " ist ungueltig oder nicht in der Organisation");
        }
        return ghUser;
    }

    public Optional<GHUser> getGhUserInOrganisation(final long gitId) throws IOException {
        return githubLogin
                .getGitHub()
                .getOrganization(organisation).listMembers().toList()
                .parallelStream()
                .filter(ghUser1 -> ghUser1.getId() == gitId).findFirst();
    }

    /*private void addUserToOrga(final GHUser ghUser) {
        try {
            final GHOrganization organization = githubLogin.gitHub.getOrganization(organisation);
            organization.add(ghUser, GHOrganization.Role.MEMBER);
        } catch (IOException ioException) {
            log.error(ioException.getMessage());
        }
    }*/

    public void removeUserFromOrga(final String username) {
        try {
            final GHOrganization organization = githubLogin.gitHub.getOrganization(organisation);
            final GHUser ghUser = githubLogin.gitHub.getUser(username);
            organization.remove(ghUser);
        } catch (IOException ioException) {
            log.error(ioException.getMessage());
        }
    }

    public void addUserToRepo(final String repo,final String user) {
        try {
            final GHOrganization organization = githubLogin.gitHub.getOrganization(organisation);
            final GHRepository repository = organization.getRepository(repo);
            final GHUser ghUser = githubLogin.gitHub.getUser(user);
            if (getGhUserInOrganisation(ghUser.getId()).isPresent() && !repository.getCollaborators().contains(ghUser)) {
                repository.addCollaborators(ghUser);
            }
        }catch(Exception exception) {
            log.error(exception.getMessage());
        }
    }

    public void removeUserFromRepo(final String repo,final String user) {
        try {
            final GHOrganization organization = githubLogin.gitHub.getOrganization(organisation);
            final GHRepository repository = organization.getRepository(repo);
            final GHUser ghUser = githubLogin.gitHub.getUser(user);
            if (ghUser.getOrganizations().contains(organization) && repository.getCollaborators().contains(ghUser)) {
                repository.removeCollaborators(ghUser);
            }
        }catch(Exception exception) {
            log.error(exception.getMessage());
        }
    }

    public void createRepo(final String repo) {
        try {

            final GHOrganization organization = githubLogin.gitHub.getOrganization(organisation);
            if(!organization.getRepositories().containsKey(repo)) {
                organization.createRepository(repo).private_(true).create();
            }
        }catch(Exception exception) {
            log.error(exception.getMessage());
        }
    }


 /*   public BenutzerTyp getUserType(String githubName) throws IOException {

        //Auslesen
        YmlConfig ymlConfig = new YmlConfig();

        System.out.println("a");
        RoleConfig rl = ymlConfig.getUserTypes();
        System.out.println(rl.getOrganisator().toString());
        System.out.println(rl.getTutoren().toString());
        if(rl.getOrganisator().contains(githubName))return BenutzerTyp.ORGANISATOR;
        if(rl.getTutoren().contains(githubName)) return BenutzerTyp.TUTOR;

        return BenutzerTyp.STUDENT;
    }*/






}
