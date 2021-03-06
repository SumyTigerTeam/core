package org.jboss.forge.addon.parser.java.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.ui.controller.CommandController;
import org.jboss.forge.addon.ui.test.UITestHarness;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.Visibility;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class JavaFieldCommandTest
{

   @Deployment
   @Dependencies({
            @AddonDependency(name = "org.jboss.forge.addon:parser-java"),
            @AddonDependency(name = "org.jboss.forge.addon:ui-test-harness"),
            @AddonDependency(name = "org.jboss.forge.addon:projects"),
            @AddonDependency(name = "org.jboss.forge.addon:maven"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
   })
   public static ForgeArchive getDeployment()
   {
      return ShrinkWrap
               .create(ForgeArchive.class)
               .addBeansXML()
               .addAsAddonDependencies(
                        AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi"),
                        AddonDependencyEntry.create("org.jboss.forge.addon:projects"),
                        AddonDependencyEntry.create("org.jboss.forge.addon:parser-java"),
                        AddonDependencyEntry.create("org.jboss.forge.addon:ui-test-harness"),
                        AddonDependencyEntry.create("org.jboss.forge.addon:maven"),
                        AddonDependencyEntry.create("org.jboss.forge.addon:ui-test-harness")
               );
   }

   @Inject
   private ProjectFactory projectFactory;

   @Inject
   private UITestHarness testHarness;

   @Inject
   private FacetFactory facetFactory;

   private Project project;

   private JavaClassSource targetClass;

   private CommandController commandController;

   @Before
   public void setup() throws Exception
   {
      createTempProject();
      createTargetClass();
      createCommandController();
   }

   @Test
   public void testGenerateFieldWithDefaults() throws Exception
   {
      commandController.initialize();
      String name = "firstName";
      setName(name);
      commandController.execute();
      reloadTargetClass();
      assertNotNull(targetClass.getField(name));
      assertTrue(targetClass.getField(name).isPrivate());
      assertNotNull(targetClass.getMethod("getFirstName"));
      assertNotNull(targetClass.getMethod("setFirstName", String.class));
   }

   @Test
   public void testGenerateFieldWithDifferentType() throws Exception
   {
      commandController.initialize();
      String age = "age";
      setName(age);
      setType("int");
      commandController.execute();
      reloadTargetClass();
      assertNotNull(targetClass.getField(age));
      assertTrue(targetClass.getField(age).isPrivate());
      assertNotNull(targetClass.getMethod("getAge"));
      assertNotNull(targetClass.getMethod("setAge", int.class));
   }

   @Test
   public void testGenerateFieldWithDifferentAccessType() throws Exception
   {
      testVisibility(Visibility.PACKAGE_PRIVATE);
      testVisibility(Visibility.PROTECTED);
      testVisibility(Visibility.PUBLIC);
   }

   private void testVisibility(Visibility visibility) throws FileNotFoundException, Exception
   {
      createTargetClass();
      commandController.initialize();
      String firstName = "firstName";
      setName(firstName);
      setAccessType(visibility);
      commandController.execute();
      reloadTargetClass();
      assertNotNull(targetClass.getField(firstName));
      assertEquals(visibility, targetClass.getField(firstName).getVisibility());
   }

   @Test
   public void testGenerateFieldWithoutGetterOrSetter() throws Exception
   {
      testGetterAndSetter(true, false);
      testGetterAndSetter(false, true);
      testGetterAndSetter(false, false);
   }

   private void testGetterAndSetter(boolean generateGetter, boolean generateSetter) throws Exception
   {
      createTargetClass();
      commandController.initialize();
      String firstName = "firstName";
      setName(firstName);
      setWithGetter(generateGetter);
      setWithSetter(generateSetter);
      commandController.execute();
      reloadTargetClass();
      assertNotNull(targetClass.getField(firstName));
      assertEquals(generateGetter, targetClass.getMethod("getFirstName") != null);
      assertEquals(generateSetter, targetClass.getMethod("setFirstName", String.class) != null);
   }

   private void createTempProject()
   {
      project = projectFactory.createTempProject();
      facetFactory.install(project, JavaSourceFacet.class);
   }

   private void createTargetClass() throws FileNotFoundException
   {
      targetClass = Roaster.parse(JavaClassSource.class, "public class Test{}");
      project.getFacet(JavaSourceFacet.class).saveJavaSource(targetClass);
   }

   private void createCommandController() throws Exception
   {
      commandController = testHarness.createCommandController(JavaFieldCommand.class,
               project.getFacet(JavaSourceFacet.class).getJavaResource(targetClass));
   }

   private void reloadTargetClass() throws FileNotFoundException
   {
      targetClass = Roaster.parse(JavaClassSource.class,
               project.getFacet(JavaSourceFacet.class).getJavaResource(targetClass)
                        .getUnderlyingResourceObject());
   }

   private void setName(String name)
   {
      commandController.setValueFor("named", name);
   }

   private void setType(String type)
   {
      commandController.setValueFor("type", type);
   }

   private void setAccessType(Visibility accessType)
   {
      commandController.setValueFor("accessType", accessType);
   }

   private void setWithGetter(boolean withGetter)
   {
      commandController.setValueFor("generateGetter", withGetter);
   }

   private void setWithSetter(boolean withSetter)
   {
      commandController.setValueFor("generateSetter", withSetter);
   }
}
