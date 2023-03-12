package testing;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import service.KVServer;
import service.HttpTaskManager;
import service.TaskManager;

import java.io.IOException;

class HttpTaskManagerTest extends TaskManagerTest {

    static TaskManager globalManager;
    static KVServer kvServer;

    @BeforeAll
    static void globalSetUp() {
        try {
            kvServer = new KVServer();
            kvServer.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String url = "http://localhost:8078";
        globalManager = new HttpTaskManager(url);
    }

    @BeforeEach
    void setUp() {
        taskManager = HttpTaskManagerTest.globalManager;
        taskManager.clearAll();
    }

    @AfterAll
    static void setDown() {
        kvServer.stop();
    }

}