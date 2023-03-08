package testing;

import org.junit.jupiter.api.BeforeEach;
import service.InMemoryTaskManager;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    @BeforeEach
    void setUp() {
        this.taskManager = new InMemoryTaskManager();
    }
}