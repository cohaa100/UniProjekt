package propraganda.praktikum.controller;

import java.util.Map;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class LoginController {


    @GetMapping("/")
    public String index(final @AuthenticationPrincipal OAuth2User principal, final Model model) {
        model.addAttribute("user",
                principal != null ?
                        principal.getAttribute("login") : null
        );

            if(principal != null && principal.getAttribute("user") != null){
                return "redirect: benutzer/organisator";
        }


        return "index";
    }
    @Secured("ROLE_ORGANISATOR")
    @RequestMapping("/user")
    public @ResponseBody
    Map<String, Object> user(final @AuthenticationPrincipal OAuth2User principal) {
        return principal.getAttributes();
    }

    @Secured("ROLE_ORGANISATOR")
    @GetMapping("/tokeninfo")
    public @ResponseBody
    Map<String, Object> tokeninfo(final @RegisteredOAuth2AuthorizedClient
                                          OAuth2AuthorizedClient authorizedClient) {
        final OAuth2AccessToken gitHubAccessToken = authorizedClient.getAccessToken();
        return Map.of("token", gitHubAccessToken);
    }


}
