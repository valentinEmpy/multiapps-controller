package com.sap.cloud.lm.sl.cf.core.dao;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.joda.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.provider.Arguments;

import com.sap.cloud.lm.sl.cf.persistence.model.ProgressMessage;
import com.sap.cloud.lm.sl.cf.persistence.model.ProgressMessage.ProgressMessageType;
import com.sap.cloud.lm.sl.common.ConflictException;

public class ProgressMessageDaoTest {

    private static final EntityManagerFactory EMF = Persistence.createEntityManagerFactory("TestDefault");
    private static final ProgressMessage PROGRESS_MESSAGE_1 = new ProgressMessage(1, "1", "taskId", ProgressMessageType.INFO, "Text");
    private static final ProgressMessage PROGRESS_MESSAGE_1_1 = new ProgressMessage(2, "1", "taskId1", ProgressMessageType.INFO, "Text1");
    private static final ProgressMessage PROGRESS_MESSAGE_2 = new ProgressMessage(2, "2", "taskId2", ProgressMessageType.INFO, "Text2");

    static Stream<Arguments> arguments = Stream.of(Arguments.of());

    @AfterEach
    public void cleanUp() {
        ProgressMessageDao dao = getDao();
        for (ProgressMessage progressMessage : dao.findAll()) {
            dao.remove(progressMessage.getId());
        }
    }

    @Test
    public void testAddNonExisting() {
        ProgressMessageDao dao = getDao();
        dao.add(PROGRESS_MESSAGE_1);
        assertNotNull(dao.find(1l));
    }

    @Test
    public void testAddAlreadyExisting() {
        ProgressMessageDao dao = getDao();
        dao.add(PROGRESS_MESSAGE_1);
        assertException(() -> dao.add(PROGRESS_MESSAGE_1), ConflictException.class,
            "Progress message for process \"1\" with ID \"1\" already exist");
    }

    public <T extends Throwable> void assertException(Executable executable, Class<T> expectedExceptionType, String message) {
        T exception = Assertions.assertThrows(expectedExceptionType, executable);
        Assertions.assertEquals(message, exception.getMessage());
    }

    @Test
    public void testAddNullProcessId() {
        ProgressMessageDao dao = getDao();
        assertException(() -> dao.add(new ProgressMessage(6, null, "taskId", ProgressMessageType.INFO, "Text")),
            IllegalStateException.class, "Progress message's \"process_id\" column value should not be null");
    }

    @Test
    public void testAddNullTaskId() {
        ProgressMessageDao dao = getDao();
        assertException(() -> dao.add(new ProgressMessage(6, "1", null, ProgressMessageType.INFO, "Text")), IllegalStateException.class,
            "Progress message's \"task_id\" column value should not be null");
    }

    @Test
    public void testAddNullType() {
        ProgressMessageDao dao = getDao();
        assertException(() -> dao.add(new ProgressMessage(6, "1", "taskId", null, "Text")), IllegalStateException.class,
            "Progress message's \"type\" column value should not be null");
    }

    @Test
    public void testAddNullText() {
        ProgressMessageDao dao = getDao();
        assertException(() -> dao.add(new ProgressMessage(6, "1", "taskId", ProgressMessageType.INFO, null)), IllegalStateException.class,
            "Progress message's \"text\" column value should not be null");
    }

    @Test
    public void testDeleteExisting() {
        ProgressMessageDao dao = getDao();
        dao.add(PROGRESS_MESSAGE_1);
        dao.remove(PROGRESS_MESSAGE_1.getId());
        Assertions.assertNull(dao.find(PROGRESS_MESSAGE_1.getId()));
    }

    @Test
    public void testUpdateExisting() {
        ProgressMessageDao dao = getDao();
        dao.add(PROGRESS_MESSAGE_1);
        dao.update(PROGRESS_MESSAGE_1.getId(), PROGRESS_MESSAGE_2);
        ProgressMessage foundProgressMessage = dao.find(PROGRESS_MESSAGE_2.getId());
        Assertions.assertEquals(PROGRESS_MESSAGE_2.getTaskId(), foundProgressMessage.getTaskId());
        Assertions.assertEquals(PROGRESS_MESSAGE_2.getText(), foundProgressMessage.getText());
    }

    @Test
    public void testFindAll() {
        ProgressMessageDao dao = getDao();
        List<ProgressMessage> progressMessages = Arrays.asList(PROGRESS_MESSAGE_1, PROGRESS_MESSAGE_1_1);
        addAll(dao, progressMessages);
        List<ProgressMessage> foundProgressMessages = dao.find(PROGRESS_MESSAGE_1.getProcessId());
        Assertions.assertEquals(progressMessages.toString(), foundProgressMessages.toString());
    }

    private void addAll(ProgressMessageDao dao, List<ProgressMessage> messages) {
        for (ProgressMessage progressMessage : messages) {
            dao.add(progressMessage);
        }
    }

    @Test
    public void testRemoveByProcessId() {
        ProgressMessageDao dao = getDao();
        List<ProgressMessage> progressMessages = Arrays.asList(PROGRESS_MESSAGE_1, PROGRESS_MESSAGE_1_1);
        addAll(dao, progressMessages);
        dao.removeBy(PROGRESS_MESSAGE_1.getProcessId());
        Assertions.assertTrue(dao.find(PROGRESS_MESSAGE_1.getProcessId())
            .isEmpty());
    }

    @Test
    public void testRemoveOlderThan() {
        ProgressMessageDao dao = getDao();
        List<ProgressMessage> progressMessages = Arrays.asList(new ProgressMessage(1, "1", "taskId1", ProgressMessageType.INFO, "Text1",
            LocalDate.now()
                .minusDays(3)
                .toDate()),
            new ProgressMessage(2, "1", "taskId2", ProgressMessageType.INFO, "Text2", LocalDate.now()
                .minusDays(3)
                .toDate()));
        addAll(dao, progressMessages);
        dao.removeOlderThan(LocalDate.now()
            .toDate());
        List<ProgressMessage> foundProgressMessages = dao.findAll();
        Assertions.assertTrue(foundProgressMessages.isEmpty());
    }

    @Test
    public void testRemoveByProcessIdTaskIdAndType() {
        ProgressMessageDao dao = getDao();
        dao.add(PROGRESS_MESSAGE_1);
        dao.removeBy(PROGRESS_MESSAGE_1.getProcessId(), PROGRESS_MESSAGE_1.getTaskId(), PROGRESS_MESSAGE_1.getType());
        Assertions.assertTrue(dao.findAll()
            .isEmpty());
    }

    private static ProgressMessageDao getDao() {
        ProgressMessageDao dao = new ProgressMessageDao();
        dao.progressMessageDtoDao = new ProgressMessageDtoDao(EMF);
        return dao;
    }
}
