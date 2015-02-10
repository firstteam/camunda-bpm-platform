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
package org.camunda.bpm.engine.test.pvm.mi;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.delegate.MultiInstanceDefinition;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;

/**
 * @author Thorben Lindhauer
 *
 */
public class ParallelMultiInstanceDefinition implements MultiInstanceDefinition {

  protected int iterations;

  public ParallelMultiInstanceDefinition(int iterations) {
    this.iterations = iterations;
  }

  public List<PvmExecutionImpl> initializeMultiInstance(PvmExecutionImpl execution) {
    List<PvmExecutionImpl> concurrentExecutions = new ArrayList<PvmExecutionImpl>();
    for (int loopCounter = 0; loopCounter < iterations; loopCounter++) {
      PvmExecutionImpl concurrentExecution = execution.createExecution(loopCounter != 0);
      concurrentExecution.setActive(true);
      concurrentExecution.setConcurrent(true);
      concurrentExecution.setScope(false);

      // In case of an embedded subprocess, and extra child execution is required
      // Otherwise, all child executions would end up under the same parent,
      // without any differentiation to which embedded subprocess they belong
//      if (isExtraScopeNeeded()) {
//        ActivityExecution extraScopedExecution = concurrentExecution.createExecution();
//        extraScopedExecution.setActive(true);
//        extraScopedExecution.setConcurrent(false);
//        extraScopedExecution.setScope(true);
//        concurrentExecution = extraScopedExecution;
//      }

      // create event subscriptions for the concurrent execution
//      for (EventSubscriptionDeclaration declaration : EventSubscriptionDeclaration.getDeclarationsForScope(execution.getActivity())) {
//        declaration.createSubscriptionForParallelMultiInstance((ExecutionEntity) concurrentExecution);
//      }

//      executeIoMapping((AbstractVariableScope) concurrentExecution);

      // create timer job for the current execution
//      createTimerJobsForExecution(concurrentExecution);

      concurrentExecutions.add(concurrentExecution);
//      logLoopDetails(concurrentExecution, "initialized", loopCounter, 0, nrOfInstances, nrOfInstances);
    }

    execution.inactivate();

    return concurrentExecutions;
  }

  public boolean completesActivity(PvmExecutionImpl execution) {
    execution.inactivate();
    // TODO: Force parent update

    List<ActivityExecution> joinedExecutions = execution.findInactiveConcurrentExecutions(execution.getActivity());
    return joinedExecutions.size() == iterations;


  }

  public PvmExecutionImpl getPropagatingExecution(PvmExecutionImpl execution) {
    return execution.getParent();
  }

  public void activityCompleted(PvmExecutionImpl propagatingExecution) {
    // Removing all active child executions (ie because completionCondition is true)
    List<ExecutionEntity> executionsToRemove = new ArrayList<ExecutionEntity>();
    for (ActivityExecution childExecution : propagatingExecution.getExecutions()) {
      if (childExecution.isActive()) {
        executionsToRemove.add((ExecutionEntity) childExecution);
      }
    }
    for (ExecutionEntity executionToRemove : executionsToRemove) {
//      if (LOGGER.isLoggable(Level.FINE)) {
//        LOGGER.fine("Execution " + executionToRemove + " still active, " + "but multi-instance is completed. Removing this execution.");
//      }
      executionToRemove.inactivate();
      executionToRemove.deleteCascade("multi-instance completed");
    }



  }

}
