package propraganda.praktikum.logic.security.github;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;
import propraganda.praktikum.logic.services.github.GithubService;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class GithubServiceTest {

    @Mock
    transient GithubLogin githubLogin;

    @InjectMocks
    transient GithubService githubService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);

        //Setzt das @Value Objekt
        ReflectionTestUtils.setField(githubService,"organisation","test");
    }

    @Test
    @DisplayName("Ist Benutzer in Orga")
    void getGitHubUserByNameInOrga() throws IOException {
        final GitHub gitHub = mock(GitHub.class);

        final GHUser ghUser = mock(GHUser.class);

        final PagedIterable<GHUser> userList = mock(PagedIterable.class);
        final GHOrganization ghOrganization = mock(GHOrganization.class);

        when(gitHub.getUser(anyString())).thenReturn(ghUser);
        when(gitHub.getOrganization(anyString())).thenReturn(ghOrganization);

        when(ghOrganization.listMembers()).thenReturn(userList);
        when(userList.toList()).thenReturn(Collections.singletonList(ghUser));

        when(ghUser.getId()).thenReturn(1L);

        given(githubLogin.getGitHub()).willReturn(gitHub);

        final GHUser result = githubService.getGitHubUserByNameInOrga("alles");

        assertThat(result).isEqualTo(ghUser);
    }

    @Test
    @DisplayName("GHUser null")
    void getGitHubUserByNameInOrgaUserNull() throws IOException {
        final GitHub gitHub = mock(GitHub.class);

        final GHUser ghUser = mock(GHUser.class);

        final PagedIterable<GHUser> userList = mock(PagedIterable.class);
        final GHOrganization ghOrganization = mock(GHOrganization.class);

        when(gitHub.getUser("test")).thenReturn(ghUser);
        when(gitHub.getOrganization(anyString())).thenReturn(ghOrganization);

        when(ghOrganization.listMembers()).thenReturn(userList);
        when(userList.toList()).thenReturn(Collections.singletonList(ghUser));

        when(ghUser.getId()).thenReturn(1L);

        given(githubLogin.getGitHub()).willReturn(gitHub);

        assertThrows(UsernameNotFoundException.class, () -> {
            githubService.getGitHubUserByNameInOrga("alles");
        });
    }

    @Test
    @DisplayName("Bekomme GhUser in Orgnisation")
    void getGhUserInOrganisation() throws IOException {
        final GitHub gitHub = mock(GitHub.class);

        final GHUser ghUser = mock(GHUser.class);

        final PagedIterable<GHUser> userList = mock(PagedIterable.class);
        final GHOrganization ghOrganization = mock(GHOrganization.class);

        when(gitHub.getUser(anyString())).thenReturn(ghUser);
        when(gitHub.getOrganization(anyString())).thenReturn(ghOrganization);

        when(ghOrganization.listMembers()).thenReturn(userList);
        when(userList.toList()).thenReturn(Collections.singletonList(ghUser));

        when(ghUser.getId()).thenReturn(1L);

        given(githubLogin.getGitHub()).willReturn(gitHub);

        assertThat(githubService.getGhUserInOrganisation(1L).get()).isEqualTo(ghUser);
    }

//    @Test
//    @DisplayName("Entferne User aus Organisation")
//    void removeGhUserOrga() throws IOException {
//        final GitHub gitHub = mock(GitHub.class);
//
//        final GHUser ghUser = mock(GHUser.class);
//
//        final PagedIterable<GHUser> userList = mock(PagedIterable.class);
//        final GHOrganization ghOrganization = mock(GHOrganization.class);
//
//        when(gitHub.getUser("test")).thenReturn(ghUser);
//        when(gitHub.getOrganization(anyString())).thenReturn(ghOrganization);
//        when(githubLogin.getGitHub()).thenReturn(gitHub);
//        when(githubLogin.getGitHub().getOrganization(anyString())).thenReturn(ghOrganization);
//        when(githubLogin.getGitHub().getUser(anyString())).thenReturn(ghUser);
//        when(ghOrganization.listMembers()).thenReturn(userList);
//        when(userList.toList()).thenReturn(Collections.singletonList(ghUser));
//
//        when(ghUser.getId()).thenReturn(1L);
//
//        given(githubLogin.getGitHub()).willReturn(gitHub);
//       githubService.removeUserFromOrga("test");
//        verify(ghOrganization, times(1)).remove(ghUser);
//    }



}