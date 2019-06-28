//package selenium;
//
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.openqa.selenium.*;
//import org.openqa.selenium.chrome.ChromeDriver;
//import org.openqa.selenium.interactions.Actions;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.TimeUnit;
//
//public class SeleniumTest {
//  private WebDriver driver;
//  private Map<String, Object> vars;
//  JavascriptExecutor js;
//  @Before
//  public void setUp() {
//
//    System.setProperty("webdriver.chrome.driver", "src/test/selenium/chromedriver.exe");
////    System.setProperty("webdriver.chrome.driver", "src/test/selenium/chromedriver_mac");
//
//    driver = new ChromeDriver();
//    driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
//    js = (JavascriptExecutor) driver;
//    vars = new HashMap<String, Object>();
//  }
//  @After
//  public void tearDown() {
//    driver.quit();
//  }
//
//  @Test
//  public void changename() {
//    driver.get("http://farm02.ewi.utwente.nl:7004/runner/logout");
//    driver.get("http://farm02.ewi.utwente.nl:7004/");
//    driver.manage().window().maximize();
//    driver.findElement(By.cssSelector(".nav-item:nth-child(2) > a")).click();
//    driver.findElement(By.name("username")).click();
//    driver.findElement(By.name("username")).sendKeys("testuser");
//    driver.findElement(By.name("password")).sendKeys("Password7");
//    driver.findElement(By.cssSelector(".button-raised > span")).click();
//    driver.findElement(By.cssSelector(".nav-item:nth-child(3) > a")).click();
//    driver.findElement(By.id("last-name-field")).click();
//    driver.findElement(By.id("last-name-field")).sendKeys("Test Complete");
//    driver.findElement(By.cssSelector("#save-my-information > span")).click();
//    driver.findElement(By.id("last-name-field")).click();
//    driver.findElement(By.id("last-name-field")).click();
//    {
//      WebElement element = driver.findElement(By.id("last-name-field"));
//      Actions builder = new Actions(driver);
//      builder.doubleClick(element).perform();
//    }
//    driver.findElement(By.id("last-name-field")).sendKeys("Test");
//    driver.findElement(By.cssSelector("#save-my-information > span")).click();
//  }
//
//  @Test
//  public void loginsuccess() {
//    driver.get("http://farm02.ewi.utwente.nl:7004/runner/logout");
//    driver.get("http://farm02.ewi.utwente.nl:7004/");
//    driver.manage().window().maximize();
//    driver.findElement(By.cssSelector(".nav-item:nth-child(2) > a")).click();
//    driver.findElement(By.name("username")).click();
//    driver.findElement(By.name("username")).sendKeys("CvdB");
//    driver.findElement(By.name("password")).sendKeys("Password7");
//    driver.findElement(By.cssSelector(".button-raised > span")).click();
//    driver.close();
//  }
//
//
//  @Test
//  public void loginfail() {
//    driver.get("http://farm02.ewi.utwente.nl:7004/runner/logout");
//    driver.get("http://farm02.ewi.utwente.nl:7004/");
//    driver.manage().window().maximize();
//    driver.findElement(By.cssSelector(".nav-item:nth-child(2) > a")).click();
//    driver.findElement(By.name("username")).click();
//    driver.findElement(By.name("username")).sendKeys("CvdB");
//    driver.findElement(By.name("password")).sendKeys("Password3");
//    driver.findElement(By.cssSelector(".button-raised > span")).click();
//    driver.close();
//  }
//  @Test
//  public void registration() {
//    driver.get("http://farm02.ewi.utwente.nl:7004/runner/logout");
//    driver.get("http://farm02.ewi.utwente.nl:7004/");
//    driver.manage().window().maximize();
//    driver.findElement(By.cssSelector(".nav-item:nth-child(1) > a")).click();
//    driver.findElement(By.id("username")).click();
//    driver.findElement(By.id("username")).sendKeys("asnddnkjdasdnadas");
//    driver.findElement(By.id("email")).sendKeys("djaskjdsajkadbaskjdbdakdsjdbk@gmail.com");
//    driver.findElement(By.id("first-name")).sendKeys("Triet");
//    driver.findElement(By.id("last-name")).sendKeys("Ngo");
//    driver.findElement(By.id("password")).sendKeys("Password7");
//    driver.findElement(By.id("confirm-password")).sendKeys("Password8");
//    driver.findElement(By.cssSelector(".button-raised > span")).click();
//    driver.findElement(By.id("confirm-password")).click();
//    driver.findElement(By.id("confirm-password")).sendKeys("Password7");
//    driver.findElement(By.cssSelector(".button-raised > span")).click();
//  }
//
//  @Test
//  public void uploadimage() {
//    driver.get("http://farm02.ewi.utwente.nl:7004/runner/logout");
//    driver.get("http://farm02.ewi.utwente.nl:7004/");
//    driver.manage().window().maximize();
//    driver.findElement(By.cssSelector(".nav-item:nth-child(2) > a")).click();
//    driver.findElement(By.name("username")).click();
//    driver.findElement(By.name("username")).sendKeys("testuser");
//    driver.findElement(By.name("password")).sendKeys("Password7");
//    driver.findElement(By.cssSelector(".button-raised > span")).click();
//    driver.findElement(By.cssSelector(".nav-item:nth-child(3) > a")).click();
//    driver.findElement(By.cssSelector("#upload-new-picture > span")).click();
//  }
//
//  @Test
//  public void filtertestwithouttoken() {
//    driver.get("http://farm02.ewi.utwente.nl:7004/runner/logout");
//    driver.get("http://farm02.ewi.utwente.nl:7004/runner/runs/1");
//    driver.manage().window().setSize(new Dimension(1920, 1050));
//    driver.findElement(By.name("username")).click();
//  }
//
//  @Test
//  public void sortingrunsinprofile() {
//    driver.get("http://farm02.ewi.utwente.nl:7004/runner/logout");
//    driver.get("http://farm02.ewi.utwente.nl:7004/");
//    driver.manage().window().maximize();
//    driver.findElement(By.cssSelector(".nav-item:nth-child(2) > a")).click();
//    driver.findElement(By.name("username")).click();
//    driver.findElement(By.name("username")).sendKeys("CvdB");
//    driver.findElement(By.name("password")).click();
//    driver.findElement(By.name("password")).sendKeys("Password7");
//    driver.findElement(By.cssSelector(".input-form")).click();
//    driver.findElement(By.cssSelector(".button-raised > span")).click();
//    driver.findElement(By.id("select-sort-type")).click();
//    {
//      WebElement dropdown = driver.findElement(By.id("select-sort-type"));
//      dropdown.findElement(By.xpath("//option[. = 'Date [A-Z]']")).click();
//    }
//    driver.findElement(By.id("select-sort-type")).click();
//    driver.findElement(By.id("select-sort-type")).click();
//    {
//      WebElement dropdown = driver.findElement(By.id("select-sort-type"));
//      dropdown.findElement(By.xpath("//option[. = 'Time [A-Z]']")).click();
//    }
//    driver.findElement(By.id("select-sort-type")).click();
//    driver.findElement(By.id("select-sort-type")).click();
//    {
//      WebElement dropdown = driver.findElement(By.id("select-sort-type"));
//      dropdown.findElement(By.xpath("//option[. = 'Time [Z-A]']")).click();
//    }
//    driver.findElement(By.id("select-sort-type")).click();
//    driver.findElement(By.id("select-sort-type")).click();
//    {
//      WebElement dropdown = driver.findElement(By.id("select-sort-type"));
//      dropdown.findElement(By.xpath("//option[. = 'Steps [A-Z]']")).click();
//    }
//    driver.findElement(By.id("select-sort-type")).click();
//    driver.findElement(By.id("select-sort-type")).click();
//    {
//      WebElement dropdown = driver.findElement(By.id("select-sort-type"));
//      dropdown.findElement(By.xpath("//option[. = 'Steps [Z-A]']")).click();
//    }
//    driver.findElement(By.id("select-sort-type")).click();
//    driver.findElement(By.id("select-sort-type")).click();
//    {
//      WebElement dropdown = driver.findElement(By.id("select-sort-type"));
//      dropdown.findElement(By.xpath("//option[. = 'Distance [A-Z]']")).click();
//    }
//    driver.findElement(By.id("select-sort-type")).click();
//    driver.findElement(By.id("select-sort-type")).click();
//    {
//      WebElement dropdown = driver.findElement(By.id("select-sort-type"));
//      dropdown.findElement(By.xpath("//option[. = 'Distance [Z-A]']")).click();
//    }
//    driver.findElement(By.id("select-sort-type")).click();
//    driver.findElement(By.id("select-sort-type")).click();
//    {
//      WebElement dropdown = driver.findElement(By.id("select-sort-type"));
//      dropdown.findElement(By.xpath("//option[. = 'Date [Z-A]']")).click();
//    }
//    driver.findElement(By.id("select-sort-type")).click();
//  }
//
//  @Test
//  public void testDashBoard() {
//    driver.get("http://farm02.ewi.utwente.nl:7004/runner/logout");
//    driver.get("http://farm02.ewi.utwente.nl:7004/");
//    driver.manage().window().maximize();
//    driver.findElement(By.cssSelector(".nav-item:nth-child(2) > a")).click();
//    driver.findElement(By.name("username")).click();
//    driver.findElement(By.name("username")).sendKeys("JF");
//    driver.findElement(By.name("password")).click();
//    driver.findElement(By.name("password")).sendKeys("Password7");
//    driver.findElement(By.cssSelector(".button-raised > span")).click();
//    driver.findElement(By.cssSelector(".run-card-see-more span")).click();
//    driver.findElement(By.cssSelector("#open-menu span")).click();
//    driver.findElement(By.cssSelector("#reset-to-default span")).click();
//    driver.findElement(By.cssSelector(".restore > span")).click();
//    driver.manage().timeouts().implicitlyWait(0, TimeUnit.MILLISECONDS);
//
//    for(int i = 0; i < 100; i++) {
//      if (!driver.findElements(By.cssSelector(".loader")).isEmpty()) {
//        try {
//          Thread.sleep(200);
//        } catch (InterruptedException e) {
//          e.printStackTrace();
//        }
//      } else {
//        break;
//      }
//    }
//    driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
//
//    driver.findElement(By.cssSelector("#open-menu span")).click();
//    driver.findElement(By.cssSelector(".fa-trash-alt")).click();
//    driver.findElement(By.cssSelector(".remove-all > span")).click();
//
//    driver.manage().timeouts().implicitlyWait(0, TimeUnit.MILLISECONDS);
//
//    for(int i = 0; i < 100; i++) {
//      if (!driver.findElements(By.cssSelector(".loader")).isEmpty()) {
//        try {
//          Thread.sleep(200);
//        } catch (InterruptedException e) {
//          e.printStackTrace();
//        }
//      }else {
//        break;
//      }
//    }
//    driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
//
//    driver.findElement(By.cssSelector("#add span")).click();
//    driver.findElement(By.cssSelector(".add > span")).click();
//    driver.findElement(By.id("select-type")).click();
//    {
//      WebElement dropdown = driver.findElement(By.id("select-type"));
//      dropdown.findElement(By.xpath("//option[. = 'Graph']")).click();
//    }
//    driver.findElement(By.id("select-type")).click();
//    driver.findElement(By.cssSelector(".add > span")).click();
//    driver.findElement(By.cssSelector(".add-dialog .button-text > span")).click();
//    driver.manage().timeouts().implicitlyWait(0, TimeUnit.MILLISECONDS);
//
//    for(int i = 0; i < 100; i++) {
//      if (!driver.findElements(By.cssSelector(".loader")).isEmpty()) {
//        try {
//          Thread.sleep(200);
//        } catch (InterruptedException e) {
//          e.printStackTrace();
//        }
//      }else {
//        break;
//      }
//    }
//
//    driver.close();
//  }
//
//}
