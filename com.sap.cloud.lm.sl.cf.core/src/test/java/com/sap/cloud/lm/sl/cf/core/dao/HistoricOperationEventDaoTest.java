package com.sap.cloud.lm.sl.cf.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.joda.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.sap.cloud.lm.sl.cf.core.model.HistoricOperationEvent;
import com.sap.cloud.lm.sl.cf.core.model.HistoricOperationEvent.EventType;
import com.sap.cloud.lm.sl.common.ConflictException;

public class HistoricOperationEventDaoTest {

    private HistoricOperationEventDao dao = createDao();
    private static final HistoricOperationEvent HISTORIC_OP_EVENT_1 = new HistoricOperationEvent.Builder("1", EventType.STARTED).id(1)
        .build();
    private static final HistoricOperationEvent HISTORIC_OP_EVENT_1_2 = new HistoricOperationEvent.Builder("1", EventType.FINISHED).id(2)
        .build();

    @Test
    public void testAdd() {
        dao.add(HISTORIC_OP_EVENT_1);
        assertNotNull(dao.find(HISTORIC_OP_EVENT_1.getId()));
    }

    @Test
    public void testAddAlreadyExisting() {
        dao.add(HISTORIC_OP_EVENT_1);
        assertException(() -> dao.add(HISTORIC_OP_EVENT_1), ConflictException.class,
            "Historic operation event for process \"1\" with ID \"1\" already exist");
    }

    public <T extends Throwable> void assertException(Executable executable, Class<T> expectedExceptionType, String message) {
        T exception = assertThrows(expectedExceptionType, executable);
        assertEquals(message, exception.getMessage());
    }

    @Test
    public void testRemove() {
        dao.add(HISTORIC_OP_EVENT_1);
        assertNotNull(dao.find(HISTORIC_OP_EVENT_1.getId()));
        dao.remove(HISTORIC_OP_EVENT_1.getId());
        assertNull(dao.find(HISTORIC_OP_EVENT_1.getId()));
    }

    @Test
    public void testUpdate() {
        dao.add(HISTORIC_OP_EVENT_1);
        dao.update(HISTORIC_OP_EVENT_1.getId(), HISTORIC_OP_EVENT_1_2);
        HistoricOperationEvent historicOperationEvent = dao.find(HISTORIC_OP_EVENT_1.getId());
        assertEquals(HISTORIC_OP_EVENT_1_2.getProcessId(), historicOperationEvent.getProcessId());
        assertEquals(HISTORIC_OP_EVENT_1_2.getType(), historicOperationEvent.getType());
    }

    @Test
    public void testFindAll() {
        List<HistoricOperationEvent> events = Arrays.asList(HISTORIC_OP_EVENT_1, HISTORIC_OP_EVENT_1_2);
        addAll(events);
        List<HistoricOperationEvent> foundEvents = dao.find(HISTORIC_OP_EVENT_1.getProcessId());
        assertEquals(events.size(), foundEvents.size());
    }

    @Test
    public void testRemoveByProcessId() {
        List<HistoricOperationEvent> events = Arrays.asList(HISTORIC_OP_EVENT_1, HISTORIC_OP_EVENT_1_2);
        addAll(events);
        dao.removeBy(HISTORIC_OP_EVENT_1.getProcessId());
        assertTrue(dao.find(HISTORIC_OP_EVENT_1.getProcessId())
            .isEmpty());
    }

    @Test
    public void testRemoveOlderThan() {
        List<HistoricOperationEvent> events = Arrays.asList(HISTORIC_OP_EVENT_1, HISTORIC_OP_EVENT_1_2);
        addAll(events);
        dao.removeOlderThan(new LocalDateTime().toDate());
        assertTrue(dao.findAll()
            .isEmpty());
    }

    private void addAll(List<HistoricOperationEvent> events) {
        for (HistoricOperationEvent event : events) {
            dao.add(event);
        }
    }

    @AfterEach
    public void cleanUp() {
        for (HistoricOperationEvent event : dao.findAll()) {
            dao.remove(event.getId());
        }
    }

    private static HistoricOperationEventDao createDao() {
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("TestDefault");
        HistoricOperationEventDao dao = new HistoricOperationEventDao();
        dao.dtoDao = new HistoricOperationEventDtoDao(entityManagerFactory);
        return dao;
    }

}
