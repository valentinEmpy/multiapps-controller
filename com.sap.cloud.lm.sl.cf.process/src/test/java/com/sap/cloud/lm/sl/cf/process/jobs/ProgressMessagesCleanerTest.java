package com.sap.cloud.lm.sl.cf.process.jobs;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sap.cloud.lm.sl.cf.core.persistence.query.ProgressMessageQuery;
import com.sap.cloud.lm.sl.cf.core.persistence.service.ProgressMessageService;

public class ProgressMessagesCleanerTest {

    private static final Date EXPIRATION_TIME = new Date(5000);

    @Mock
    private ProgressMessageService progressMessageService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ProgressMessageQuery messageQuery;
    @InjectMocks
    private ProgressMessagesCleaner cleaner;

    @BeforeEach
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        when(progressMessageService.createQuery()).thenReturn(messageQuery);
    }

    @Test
    public void testExecute() {
        cleaner.execute(EXPIRATION_TIME);
        verify(messageQuery.olderThan(EXPIRATION_TIME)).delete();
    }

}
