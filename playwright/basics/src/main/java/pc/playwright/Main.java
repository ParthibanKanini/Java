package pc.playwright;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.BrowserType.LaunchOptions;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.Locator;

public class Main {

    private LaunchOptions getLaunchOptions() {
        // TODO: Read from the config file
        boolean isHeadless = false;
        LaunchOptions launchOptions = new BrowserType.LaunchOptions().setHeadless(isHeadless);
        return launchOptions;
    }

    private Browser getBrowser(Playwright playwright) {
        Browser browser = null;
        LaunchOptions launchOptions = getLaunchOptions();
        // TODO: Read from the config file
        String testBrowserName = "chrome";
        switch (testBrowserName) {
            case "chrome":
            case "msedge":
            case "chrome-beta":
            case "msedge-beta":
            case "msedge-dev":
            case "chromium":
                browser = playwright.chromium().launch(launchOptions);
                break;
            case "firefox":
                browser = playwright.firefox().launch(launchOptions);
                break;
            case "webkit":
                browser = playwright.webkit().launch(launchOptions);
                break;
            default:
                break;
        }
        return browser;
    }

    /**
     * Using frame locators to find elements within a frame (or) iframe.
     * 
     * @param pageURL
     */
    private void usingFrameLocators(String pageURL) {
        try (final Playwright playwright = Playwright.create(); final Browser browser = getBrowser(playwright);) {
            if (browser != null) {
                BrowserContext newContext = browser.newContext();
                try (Page appPage = newContext.newPage();) {
                    appPage.navigate(pageURL);
                    // using css selector in frameLocator.
                    String headerTxt = appPage.frameLocator("frame[name='main']").locator("h2").textContent();
                    System.out.println("CSS selector in frameLocator :" + headerTxt);

                    // using xpath selector in frameLocator.
                    headerTxt = appPage.frameLocator("//frame[@name='main']").locator("h2").textContent();
                    System.out.println("XPATH selector in frameLocator :" + headerTxt);

                    // Alternatively use name of frame
                    headerTxt = appPage.frame("main").locator("h2").textContent();
                    System.out.println("frame + locator :" + headerTxt);

                }
            }
        }
    }

    private void usingBasicSelectors(String pageURL) {
        try (final Playwright playwright = Playwright.create(); final Browser browser = getBrowser(playwright);) {
            if (browser != null) {
                BrowserContext newContext = browser.newContext();
                try (Page appPage = newContext.newPage();) {

                    // Uncomment to pause execution and inspect the page in Playwright DevTools
                    // appPage.pause();

                    appPage.navigate(pageURL);

                    // LOCATORS: a way to find element(s) on the page at any moment in time.
                    // https://playwright.dev/java/docs/locators

                    // getByAltText: Finds an element by alt text.
                    appPage.getByAltText("Website for automation practice").click();

                    // getByRole: Finds an element by aria role.
                    Locator signUpLocator = appPage.getByRole(AriaRole.LINK,
                            new Page.GetByRoleOptions().setName("Signup / Login"));
                    signUpLocator.click();

                    // CSS or XPath

                    // Use text locators to find non interactive elements like div, span, p, etc.
                    // For interactive elements like button, a, input, etc. use role locators.
                    boolean registrationSectionVisible = appPage.getByText("New User Signup!").isVisible();
                    System.out.println("Registration Section is visible: " + registrationSectionVisible);

                    // getByPlaceholder: Finds an element by placeholder attribute value.
                    appPage.getByPlaceholder("Name").fill("TestTeaserUser1");

                    // locator: Finds an element by CSS selector or XPath expression.
                    // xpath= or css= are optional. xpaths can start with // and css just the
                    // string.
                    // Bad practice to use long chains like
                    // xpath=//*[@id=\"form\"]/div/div/div[3]/div/form/input[2]
                    // appPage.locator("xpath=//*[@id=\"form\"]/input[2]").fill("TestTeaserUser1");
                    appPage.locator("xpath=//form[@action='/signup']/input[@name='email']")
                            .fill("testTeaser1@test.com");

                    appPage.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Signup")).click();

                    //// Get Webelement in the webpage. first() will allow to narrow down to the
                    //// first element in the page.
                    // Locator productsLocator = page.locator("text= Signup / Login").first();
                    // productsLocator.click();

                    // getByLabelText: Finds an element by label text.
                    // <label>Name <input type="text" /></label>
                    // appPage.getByLabel("Name").fill("TestTeaserUser1");

                    // getByTestId: Agree and define explicit test ids and query them. By default,
                    // use data-testid as attribute.
                    // use a custom data attribute with setTestIdAttribute method.
                    // <button data-testid-custom="directions">Road Way</button>
                    // playwright.selectors().setTestIdAttribute("data-testid-custom");
                    // page.getByTestId("directions").click();

                    // getByText: Finds an element by text content.
                    // Use text locators to find non interactive elements like div, span, p, etc.
                    // For interactive elements like button, a, input, etc. use role locators.

                    // getByTitle: Finds an element by title attribute value.
                    // <span title='Issues count'>25 issues</span>
                    // assertThat(page.getByTitle("Issues count")).hasText("25 issues");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Browser configured is not supported. Please check test Browser configuration.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void shadowDOMControls(String pageURL) {
        // Playwright can only interact with the shadow DOM elements if the shadowRoot is open.
        try (final Playwright playwright = Playwright.create(); final Browser browser = getBrowser(playwright);) {
            if (browser != null) {
                BrowserContext newContext = browser.newContext();
                try (Page appPage = newContext.newPage();) {
                    appPage.navigate(pageURL);

                    ;
                }
            }
        }
        ;
    }

    public static void main(String[] args) {

        Main main = new Main();
        // main.usingBasicSelectors("https://www.automationexercise.com/");
        main.usingFrameLocators("https://www.londonfreelance.org/courses/frames/index.html");

    }

}

/**
 * Application under test.
 * 
 * https://www.automationexercise.com/
 * 
 * testKanini@test.com
 * testKanini
 * 
 */