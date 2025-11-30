package com.java.automation.base;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.java.automation.config.TestConfig;
import com.java.automation.utils.ExtentReportManager;
import com.java.automation.utils.LoggerUtil;
import com.java.automation.utils.ScreenshotUtil;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import java.util.concurrent.TimeUnit;

/**
 * Base test class that sets up and tears down WebDriver
 * Includes Extent Reports, Logging, and Screenshot capabilities
 */
public class BaseTest {
    protected WebDriver driver;
    protected Logger logger;
    protected ExtentTest extentTest;

    @BeforeSuite
    public void setUpSuite() {
        // Initialize Extent Reports
        ExtentReportManager.getInstance();
        logger = LoggerUtil.getLogger(this.getClass());
        logger.info("Test Suite started");
    }

    @BeforeMethod
    public void setUp(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String testDescription = result.getMethod().getDescription();
        if (testDescription == null || testDescription.isEmpty()) {
            testDescription = "Test: " + testName;
        }
        
        // Create test in Extent Report
        extentTest = ExtentReportManager.createTest(testName, testDescription);
        
        logger = LoggerUtil.getLogger(this.getClass());
        logger.info("Starting test: " + testName);
        String browser = TestConfig.getBrowser().toLowerCase();
        
        switch (browser) {
            case "chrome":
                WebDriverManager.chromedriver().setup();
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments("--start-maximized");
                chromeOptions.addArguments("--disable-notifications");
                driver = new ChromeDriver(chromeOptions);
                break;
                
            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                firefoxOptions.addArguments("--start-maximized");
                driver = new FirefoxDriver(firefoxOptions);
                break;
                
            case "edge":
                WebDriverManager.chromedriver().setup();
                ChromeOptions options = new ChromeOptions();
                options.addArguments("--start-maximized");
                options.addArguments("--disable-notifications");
                driver = new ChromeDriver(options);
                break;
                
            default:
                WebDriverManager.chromedriver().setup();
                ChromeOptions defaultOptions = new ChromeOptions();
                defaultOptions.addArguments("--start-maximized");
                driver = new ChromeDriver(defaultOptions);
        }

        driver.manage().timeouts().implicitlyWait(TestConfig.getImplicitWait(), TimeUnit.SECONDS);
        driver.manage().timeouts().pageLoadTimeout(TestConfig.getPageLoadTimeout(), TimeUnit.SECONDS);
        driver.get(TestConfig.getBaseUrl());
        
        logger.info("Navigated to: " + TestConfig.getBaseUrl());
        extentTest.log(Status.INFO, "Navigated to: " + TestConfig.getBaseUrl());
    }

    @AfterMethod
    public void tearDown(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        
        // Log test result
        if (result.getStatus() == ITestResult.FAILURE) {
            logger.error("Test FAILED: " + testName);
            extentTest.log(Status.FAIL, "Test Failed: " + result.getThrowable().getMessage());
            
            // Take screenshot on failure
            String screenshotPath = ScreenshotUtil.takeScreenshot(driver, testName + "_FAILED");
            if (screenshotPath != null) {
                try {
                    extentTest.addScreenCaptureFromPath(screenshotPath);
                    logger.info("Screenshot saved: " + screenshotPath);
                } catch (Exception e) {
                    logger.error("Error adding screenshot to report: " + e.getMessage());
                }
            }
        } else if (result.getStatus() == ITestResult.SUCCESS) {
            logger.info("Test PASSED: " + testName);
            extentTest.log(Status.PASS, "Test Passed");
        } else if (result.getStatus() == ITestResult.SKIP) {
            logger.warn("Test SKIPPED: " + testName);
            extentTest.log(Status.SKIP, "Test Skipped");
        }
        
        // Close browser
        if (driver != null) {
            driver.quit();
            logger.info("Browser closed");
        }
        
        // Flush Extent Report
        ExtentReportManager.flush();
    }
}

