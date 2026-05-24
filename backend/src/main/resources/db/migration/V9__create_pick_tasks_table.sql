CREATE TABLE pick_tasks (
    id BIGSERIAL PRIMARY KEY,
    allocation_id BIGINT NOT NULL REFERENCES order_allocations(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    assigned_to BIGINT REFERENCES users(id) ON DELETE SET NULL,
    picked_qty INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_pick_tasks_allocation_id ON pick_tasks(allocation_id);
CREATE INDEX idx_pick_tasks_status ON pick_tasks(status);
CREATE INDEX idx_pick_tasks_assigned_to ON pick_tasks(assigned_to);
