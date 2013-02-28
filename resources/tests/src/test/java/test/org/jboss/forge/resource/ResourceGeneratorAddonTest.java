package test.org.jboss.forge.resource;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.Addon;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.container.AddonDependency;
import org.jboss.forge.container.AddonId;
import org.jboss.forge.resource.ResourceFactory;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@RunWith(Arquillian.class)
public class ResourceGeneratorAddonTest
{
   @Deployment(order = 1)
   @Dependencies({ @Addon(name = "org.jboss.forge:resources", version = "2.0.0-SNAPSHOT") })
   public static ForgeArchive getDeployment()
   {
      ForgeArchive archive = ShrinkWrap
               .create(ForgeArchive.class)
               .addBeansXML()
               .addAsAddonDependencies(
                        AddonDependency.create(AddonId.from("org.jboss.forge:resources", "2.0.0-SNAPSHOT")),
                        AddonDependency.create(AddonId.from("mockstring", "1")));

      return archive;
   }

   @Deployment(testable = false, name = "mockstring,1", order = 3)
   public static ForgeArchive getAddonDeployment()
   {
      ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
               .addClasses(MockStringResource.class, MockStringResourceGenerator.class)
               .addBeansXML()
               .addAsAddonDependencies(
                        AddonDependency.create(AddonId.from("org.jboss.forge:resources", "2.0.0-SNAPSHOT"))
               );

      return archive;
   }

   @Inject
   private ResourceFactory factory;

   @Test
   public void testCreateResourceFromAddon() throws Exception
   {
      Assert.assertNotNull(factory);
      MockStringResource resource = (MockStringResource) factory.create("It's a valid string!");
      Assert.assertNotNull(resource);
      Assert.assertEquals("It's a valid string!", resource.getUnderlyingResourceObject());
   }

   @Test
   public void testCreateUnhandledResourceFromAddon() throws Exception
   {
      Assert.assertNotNull(factory);
      MockStringResource resource = (MockStringResource) factory.create("It's a bad string!");
      Assert.assertNull(resource);
   }

}