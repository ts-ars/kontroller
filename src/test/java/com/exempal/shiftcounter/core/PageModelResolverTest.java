package com.exempal.shiftcounter.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@org.junit.jupiter.api.Tag("unit")
class PageModelResolverTest {

    private PageModelResolver resolver;
    private PageModel dummyPage;

    @BeforeEach
    void setUp() {
        dummyPage = mock(PageModel.class);
        when(dummyPage.getPageName()).thenReturn("dummy");

        resolver = new PageModelResolver(List.of(dummyPage));
    }

    @Test
    void resolve_shouldPopulateModelAndReturnLayout() {
        // given
        Model model = new ConcurrentModel();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameterMap()).thenReturn(Map.of("x", new String[]{"y"}));

        // when
        String result = resolver.resolve("dummy", model, request);

        // then
        assertThat(result).isEqualTo("layout");
        assertThat(model.getAttribute("currentPage")).isEqualTo("dummy");
        assertThat(model.getAttribute("pageTitle")).isEqualTo("Dummy");
        assertThat(model.getAttribute("contentTemplate")).isEqualTo("features/dummy/dummy");
        verify(dummyPage).populateModel(eq(model), eq(Map.of("x", "y")));
    }

    @Test
    void resolve_shouldThrow404IfPageNotFound() {
        Model model = new ConcurrentModel();
        HttpServletRequest request = mock(HttpServletRequest.class);

        assertThatThrownBy(() -> resolver.resolve("missing", model, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Page not found: missing");
    }
}
