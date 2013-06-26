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
package org.camunda.bpm.integrationtest.functional.spring;

import org.camunda.bpm.integrationtest.functional.spring.beans.ExampleBean;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.camunda.bpm.integrationtest.util.DeploymentHelper;
import org.camunda.bpm.integrationtest.util.TestContainer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * <p>Integration test that makes sure the shared container managed process engine is able to resolve 
 * Spring beans form a spring process application</p>
 * 
 * @author Daniel Meyer
 *
 */
@RunWith(Arquillian.class)
public class SpringPAExpressionResolvingTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive processArchive() {    
    
    // deploy spring Process Application (does not include ejb-client nor cdi modules)
    return ShrinkWrap.create(WebArchive.class, "test.war")
        
      // add example bean to serve as JavaDelegate
      .addClass(ExampleBean.class)
      // add process definitions
      .addAsResource("org/camunda/bpm/integrationtest/functional/spring/SpringExpressionResolvingTest.testResolveBean.bpmn20.xml")
      .addAsResource("org/camunda/bpm/integrationtest/functional/spring/SpringExpressionResolvingTest.testResolveBeanFromJobExecutor.bpmn20.xml")
      
      // regular deployment descriptor
      .addAsResource("META-INF/processes.xml", "META-INF/processes.xml")
      
      // web xml that bootstrapps spring
      .addAsWebInfResource("org/camunda/bpm/integrationtest/functional/spring/web.xml", "web.xml")
      
      // spring application context & libs
      .addAsWebInfResource("org/camunda/bpm/integrationtest/functional/spring/SpringPAExpressionResolvingTest-context.xml", "applicationContext.xml")
      .addAsLibraries(DeploymentHelper.getEngineSpring());         
  }
  

  @Deployment(name="clientDeployment")
  public static WebArchive clientDeployment() {    
    
    // the test is deployed as a seperate deployment
    
    WebArchive deployment = ShrinkWrap.create(WebArchive.class, "client.war")
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
            .addClass(AbstractFoxPlatformIntegrationTest.class)
            .addAsLibraries(DeploymentHelper.getEngineCdi());
    
    TestContainer.addContainerSpecificResourcesForNonPa(deployment);
    
    return deployment;
  }

  @Test
  @OperateOnDeployment("clientDeployment")
  public void testResolveBean() {       
    Assert.assertEquals(0,runtimeService.createProcessInstanceQuery().count());
    // but the process engine can:
    runtimeService.startProcessInstanceByKey("testResolveBean");
    
    Assert.assertEquals(0,runtimeService.createProcessInstanceQuery().count());
  }
  
  @Test
  @OperateOnDeployment("clientDeployment")
  public void testResolveBeanFromJobExecutor() {
   
    Assert.assertEquals(0,runtimeService.createProcessInstanceQuery().count());
    runtimeService.startProcessInstanceByKey("testResolveBeanFromJobExecutor");
    Assert.assertEquals(1,runtimeService.createProcessInstanceQuery().count());
    
    waitForJobExecutorToProcessAllJobs(16000, 500);    
    
    Assert.assertEquals(0,runtimeService.createProcessInstanceQuery().count());    
    
  }
  
}