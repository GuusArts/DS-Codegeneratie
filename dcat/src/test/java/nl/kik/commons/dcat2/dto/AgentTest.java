package nl.kik.commons.dcat2.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AgentTest {
    @Test
    public void testAgentFields() {
        Agent agent = new Agent();
        agent.setId("agent1");
        agent.setName("Test Agent");

        assertEquals("agent1", agent.getId());
        assertEquals("Test Agent", agent.getName());
    }
} 