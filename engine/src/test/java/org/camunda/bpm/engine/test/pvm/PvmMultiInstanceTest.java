/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.pvm;

import java.util.List;

import org.camunda.bpm.engine.impl.pvm.ProcessDefinitionBuilder;
import org.camunda.bpm.engine.impl.pvm.PvmExecution;
import org.camunda.bpm.engine.impl.pvm.PvmProcessDefinition;
import org.camunda.bpm.engine.impl.pvm.PvmProcessInstance;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;
import org.camunda.bpm.engine.impl.test.PvmTestCase;
import org.camunda.bpm.engine.test.pvm.activities.Automatic;
import org.camunda.bpm.engine.test.pvm.activities.End;
import org.camunda.bpm.engine.test.pvm.activities.WaitState;
import org.camunda.bpm.engine.test.pvm.mi.ParallelMultiInstanceDefinition;

/**
 * @author Thorben Lindhauer
 *
 */
public class PvmMultiInstanceTest extends PvmTestCase {

  public void testSimpleParallelMultiInstance() {
    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .transition("task")
      .endActivity()
      .createActivity("task")
        .behavior(new WaitState())
        .multiInstanceDefinition(new ParallelMultiInstanceDefinition(5))
        .transition("end")
      .endActivity()
      .createActivity("end")
        .behavior(new End())
      .endActivity()
    .buildProcessDefinition();

    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();

    List<String> activeActivityIds = processInstance.findActiveActivityIds();
    assertEquals(5, activeActivityIds.size());

    List<PvmExecution> activityInstancesTask = processInstance.findExecutions("task");
    assertNotNull(activityInstancesTask);

    assertEquals(6, activityInstancesTask.size());

    for (PvmExecution execution : activityInstancesTask) {

      assertEquals("task", execution.getActivity().getId());
    }

    int signalledExecutions = 0;
    for (int i = 0; i < 6; i++) {
      PvmExecution execution = activityInstancesTask.get(i);

      // don't signal the concurrent root
      if (((PvmExecutionImpl) execution).isActive()) {
        execution.signal(null, null);
        signalledExecutions++;

        if (signalledExecutions < 5) {
          assertFalse(processInstance.isEnded());
        }
      }
    }

    assertTrue(processInstance.isEnded());
  }
}
