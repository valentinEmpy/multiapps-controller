package com.sap.cloud.lm.sl.cf.core.model;

public class PersistenceMetadata {

    public static final String NOT_AVAILABLE = "N/A";

    public static class NamedQueries {

        public static final String DELETE_EXPIRED_OPERATIONS_IN_FINAL_STATE = "remove_expired_operations_in_final_state";
        public static final String FIND_ALL_OPERATIONS = "find_all_operations";

        public static final String FIND_ALL_ENTRIES = "find_all_entries";
        public static final String FIND_ALL_ENTRIES_BY_SPACE_ID = "find_all_entries_guid";
        public static final String FIND_ALL_SUBSCRIPTIONS = "find_all_subscriptions";
        public static final String FIND_ALL_SUBSCRIPTIONS_BY_SPACE_ID = "find_all_subscriptions_guid";

        public static final String FIND_ALL_PROGRESS_MESSAGES = "find_all_progress_messages";
        public static final String FIND_PROGRESS_MESSAGES_BY_PROCESS_ID = "find_progress_messages_by_process_id";
        public static final String DELETE_PROGRESS_MESSAGES_BY_PROCESS_ID = "delete_progress_messages_by_process_id";
        public static final String DELETE_PROGRESS_MESSAGES_OLDER_THAN = "delete_progress_messages_older_than";
        public static final String DELETE_PROGRESS_MESSAGES_BY_PROCESS_AND_TASK_ID_AND_TYPE = "delete_progress_messages_by_process_id_and_task_id_and_type";

        public static final String FIND_ALL_HISTORIC_OPERATION_EVENTS = "find_all_historic_operation_events";
        public static final String FIND_HISTORIC_OPERATION_EVENTS_BY_PROCESS_ID = "find_historic_operation_events_by_process_id";
        public static final String DELETE_HISTORIC_OPERATION_EVENTS_BY_PROCESS_ID = "delete_historic_operation_events_by_process_id";
        public static final String DELETE_HISTORIC_OPERATION_EVENTS_OLDER_THAN = "delete_historic_operation_events_older_than";

    }

    public static class QueryParameters {

        public static final String MTA_ID = "mtaId";
        public static final String LAST_QUERY_PARAM = "last";
        public static final String STATUS_QUERY_PARAM = "status";

    }

    public static class TableNames {

        public static final String CONFIGURATION_ENTRY_TABLE = "configuration_registry";
        public static final String CONFIGURATION_SUBSCRIPTION_TABLE = "configuration_subscription";
        public static final String PROGRESS_MESSAGE_TABLE = "progress_message";
        public static final String HISTORIC_OPERATION_EVENT_TABLE = "historic_operation_event";

    }

    public static class SequenceNames {

        public static final String DEPLOY_TARGET_SEQUENCE = "deploy_target_sequence";
        public static final String CONFIGURATION_ENTRY_SEQUENCE = "configuration_entry_sequence";
        public static final String CONFIGURATION_SUBSCRIPTION_SEQUENCE = "configuration_subscription_sequence";
        public static final String PROGRESS_MESSAGE_SEQUENCE = "ID_SEQ";
        public static final String HISTORIC_OPERATION_EVENT_SEQUENCE = "historic_operation_event_sequence";

    }

    public static class TableColumnNames {

        public static final String CONFIGURATION_ENTRY_ID = "id";
        public static final String CONFIGURATION_ENTRY_PROVIDER_NID = "provider_nid";
        public static final String CONFIGURATION_ENTRY_PROVIDER_ID = "provider_id";
        public static final String CONFIGURATION_ENTRY_PROVIDER_VERSION = "provider_version";
        public static final String CONFIGURATION_ENTRY_TARGET_ORG = "target_org";
        public static final String CONFIGURATION_ENTRY_TARGET_SPACE = "target_space";
        public static final String CONFIGURATION_ENTRY_SPACE_ID = "space_id";
        public static final String CONFIGURATION_ENTRY_CONTENT = "content";
        public static final String CONFIGURATION_CLOUD_TARGET = "visibility";

        public static final String CONFIGURATION_SUBSCRIPTION_MTA_ID = "mta_id";
        public static final String CONFIGURATION_SUBSCRIPTION_ID = "id";
        public static final String CONFIGURATION_SUBSCRIPTION_SPACE_ID = "space_id";
        public static final String CONFIGURATION_SUBSCRIPTION_APP_NAME = "app_name";
        public static final String CONFIGURATION_SUBSCRIPTION_RESOURCE_PROP = "resource_properties";
        public static final String CONFIGURATION_SUBSCRIPTION_RESOURCE_NAME = "resource_name";
        public static final String CONFIGURATION_SUBSCRIPTION_MODULE = "module";
        public static final String CONFIGURATION_SUBSCRIPTION_FILTER = "filter";

        public static final String ONGOING_OPERATION_SPACE_ID = "spaceId";
        public static final String ONGOING_OPERATION_STARTED_AT = "startedAt";
        public static final String ONGOING_OPERATION_FINAL_STATE = "finalState";
        public static final String ONGOING_OPERATION_MTA_ID = "mtaId";

        public static final String DEPLOY_TARGET_NAME = "name";

        public static final String PROGRESS_MESSAGE_ID = "id";
        public static final String PROGRESS_MESSAGE_PROCESS_ID = "process_id";
        public static final String PROGRESS_MESSAGE_TASK_ID = "task_id";
        public static final String PROGRESS_MESSAGE_TYPE = "type";
        public static final String PROGRESS_MESSAGE_TEXT = "text";
        public static final String PROGRESS_MESSAGE_TIMESTAMP = "timestamp";

        public static final String HISTORIC_OPERATION_EVENT_ID = "id";
        public static final String HISTORIC_OPERATION_EVENT_PROCESS_ID = "process_id";
        public static final String HISTORIC_OPERATION_EVENT_TYPE = "event";
        public static final String HISTORIC_OPERATION_EVENT_TIMESTAMP = "timestamp";

    }

}
