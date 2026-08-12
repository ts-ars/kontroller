package com.exempal.shiftcounter.core;
import com.exempal.shiftcounter.features.user.adapter.security.LocalPinAuthenticationProvider;
import org.junit.jupiter.api.Test; import org.junit.jupiter.api.BeforeEach; import org.springframework.beans.factory.annotation.Autowired; import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest; import org.springframework.context.annotation.Import; import org.springframework.boot.test.mock.mockito.MockBean; import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.*; import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user; import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get; import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(controllers=PageController.class) @Import(SecurityConfiguration.class)
class SecurityContractMvcTest {@Autowired MockMvc mvc; @MockBean PageModelResolver resolver; @MockBean LocalPinAuthenticationProvider provider;
 @BeforeEach void controllerBoundary(){when(resolver.resolve(anyString(),any(),any())).thenReturn("redirect:/signin");}
 @Test void anonymousRequestRedirectsToSignIn() throws Exception {mvc.perform(get("/page/shift")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrlPattern("**/signin"));}
 @Test void userCannotOpenSettings() throws Exception {mvc.perform(get("/page/settings").with(user("Sam").roles("USER"))).andExpect(status().isForbidden());}
 @Test void adminCanReachSettingsBoundary() throws Exception {mvc.perform(get("/page/settings").with(user("Ada").roles("ADMIN"))).andExpect(status().is3xxRedirection());}}
