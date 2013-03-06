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

package org.camunda.bpm.engine.impl.cmd;

import java.io.Serializable;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.DbSqlSession;
import org.camunda.bpm.engine.impl.form.TaskFormHandler;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricFormPropertyEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class SubmitTaskFormCmd implements Command<Object>, Serializable {

  private static final long serialVersionUID = 1L;
  protected String taskId;
  protected Map<String, String> properties;
  
  public SubmitTaskFormCmd(String taskId, Map<String, String> properties) {
    this.taskId = taskId;
    this.properties = properties;
  }

  public Object execute(CommandContext commandContext) {
    if(taskId == null) {
      throw new ProcessEngineException("taskId is null");
    }
    
    TaskEntity task = Context
      .getCommandContext()
      .getTaskManager()
      .findTaskById(taskId);

    if (task == null) {
      throw new ProcessEngineException("Cannot find task with id " + taskId);
    }
    
    int historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();
    ExecutionEntity execution = task.getExecution();
    if (historyLevel>=ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT && execution != null) {
      DbSqlSession dbSqlSession = commandContext.getSession(DbSqlSession.class);
      for (String propertyId: properties.keySet()) {
        String propertyValue = properties.get(propertyId);
        HistoricFormPropertyEntity historicFormProperty = new HistoricFormPropertyEntity(execution, propertyId, propertyValue, taskId);
        dbSqlSession.insert(historicFormProperty);
      }
    }
    
    TaskFormHandler taskFormHandler = task.getTaskDefinition().getTaskFormHandler();
    taskFormHandler.submitFormProperties(properties, task.getExecution());
    
    task.complete();

    return null;
  }
}