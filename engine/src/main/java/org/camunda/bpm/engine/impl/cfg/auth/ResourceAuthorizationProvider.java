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
package org.camunda.bpm.engine.impl.cfg.auth;

import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationEntity;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;

/**
 *
 * @author Daniel Meyer
 *
 */
public interface ResourceAuthorizationProvider {

  // Users /////////////////////////////////////////////

  /**
   * <p>Invoked whenever a new user is created</p>
   *
   * @param user
   *          a newly created user
   * @return a list of authorizations to be automatically added when a new user
   *         is created.
   */
  public AuthorizationEntity[] newUser(User user);

  /**
   * <p>Invoked whenever a new group is created</p>
   *
   * @param user
   *          a newly created {@link User}
   * @return a list of authorizations to be automatically added when a new
   *         {@link User} is created.
   */
  public AuthorizationEntity[] newGroup(Group group);

  /**
   * <p>Invoked whenever a user is added to a group</p>
   *
   * @param userId
   *          the id of the user who is added to a group a newly created
   *          {@link User}
   * @param groupId
   *          the id of the group to which the user is added
   * @return a list of authorizations to be automatically added when a new
   *         {@link User} is created.
   */
  public AuthorizationEntity[] groupMembershipCreated(String groupId, String userId);

  // Filter ////////////////////////////////////////////////

  /**
   * <p>Invoked whenever a new filter is created</p>
   *
   * @param filter the newly created filter
   * @return a list of authorizations to be automatically added when a new
   *         {@link Filter} is created.
   */
  public AuthorizationEntity[] newFilter(Filter filter);

  // Process Definition //////////////////////////////////////

  /**
   * <p>Invoked whenever a new process definition is created</p>
   *
   * @param processDefinition the newly created process definition
   * @return a list of authorizations to be automatically added when a new
   *         {@link ProcessDefinition} is created.
   */
  public AuthorizationEntity[] newProcessDefinition(ProcessDefinition processDefinition);

  // Process Instance ///////////////////////////////////////

  /**
   * <p>Invoked whenever a new process instance is started</p>
   *
   * @param processInstance the newly started process instance
   * @return a list of authorizations to be automatically added when a new
   *         {@link ProcessInstance} is started.
   */
  public AuthorizationEntity[] newProcessInstance(ProcessInstance processInstance);

  // Task /////////////////////////////////////////////////

  /**
   * <p>Invoked whenever a new task is created</p>
   *
   * @param task the newly created task
   * @return a list of authorizations to be automatically added when a new
   *         {@link Task} is created.
   */
  public AuthorizationEntity[] newTask(Task task);

}
