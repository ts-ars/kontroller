package com.exempal.shiftcounter.architecture;

import org.junit.jupiter.api.Test;
import java.nio.file.*;
import static org.assertj.core.api.Assertions.assertThat;

class CommentAuthorshipContractTest {
    @Test void migrationBackfillsLegacyAndDefinesAuditForeignKeys() throws Exception {
        String sql = Files.readString(Path.of("src/main/resources/db/migration/V12__add_comment_authorship.sql"));
        assertThat(sql).contains("'Legacy'", "author_user_id UUID", "created_at TIMESTAMP WITH TIME ZONE",
                "updated_at TIMESTAMP WITH TIME ZONE", "last_modified_by UUID",
                "SET author_user_id = '00000000-0000-0000-0000-000000000001'",
                "fk_loss_explanation_author", "fk_loss_explanation_last_modifier");
    }

    @Test void commentsUiPlacesAuthorImmediatelyAfterActionsAndKeepsLiveRefresh() throws Exception {
        String html = Files.readString(Path.of("src/main/resources/templates/features/comment/comment.html"));
        assertThat(html).contains("<th>Actions</th><th>Author</th>",
                "client.subscribe(`/topic/comments/${selectedCommentsSensor}`, refreshComments)",
                "client.subscribe(`/topic/shift-updates/${selectedCommentsSensor}`, refreshComments)",
                "row.querySelector('.author').textContent = saved.authorDisplayName");
        assertThat(html).doesNotContain("Комментарий дал");
    }

    @Test void requestContractHasNoClientControlledAuthorship() throws Exception {
        String request = Files.readString(Path.of("src/main/java/com/exempal/shiftcounter/features/comment/adapter/dto/LossExplanationRequest.java"));
        assertThat(request).doesNotContain("authorUserId", "authorDisplayName", "lastModifiedBy");
    }
}
