CREATE TABLE notifications (
   event_id UUID PRIMARY KEY,
   correlation_id UUID,
   core_item_id UUID,
   owner_user_id UUID,
   payload TEXT,
   created_at  TIMESTAMP NOT NULL
);