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
package org.camunda.bpm.engine.impl.pvm.runtime.operation;

import org.camunda.bpm.engine.impl.pvm.delegate.MultiInstanceDefinition;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;

/**
 * @author Thorben Lindhauer
 *
 */
public class PvmAtomicOperationMultiInstanceComplete implements PvmAtomicOperation {

  public void execute(PvmExecutionImpl execution) {
    ActivityImpl activity = execution.getActivity();
    MultiInstanceDefinition miDefinition = activity.getMultiInstanceDefinition();

    // TODO: Ist das gut das gemeinsame Verhalten so in drei Methoden zu teilen?
    if (miDefinition.completesActivity(execution)) {
      PvmExecutionImpl propagatingExecution = miDefinition.getPropagatingExecution(execution);
      propagatingExecution.setTransition(execution.getTransition());
      miDefinition.activityCompleted(propagatingExecution);
      propagatingExecution.performOperation(TRANSITION_DESTROY_SCOPE);
    } else {
      // TODO: how to synchronize here? lock concurrent root? should be part of mi definition?
    }

    // TODO: falls sequenziell muss man wieder zu MI_INITIALIZE hoch
  }

  public boolean isAsync(PvmExecutionImpl execution) {
    return false;
  }

  public String getCanonicalName() {
    return "multi-instance-init";
  }

}
