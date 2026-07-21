# Assignment #2

Java project using Selenium 4 automating the following scenario.

Create a light user account at <https://wallethub.com/join/light> and **uncheck**
the checkbox to get your free credit score and report. Use this account for the
test. Then:

1. Go to the profile URL: <https://wallethub.com/profile/13732055i>
2. In the reviews section, hover over the stars and click the **fourth** star.
   The code should actually (a) perform the hover and (b) verify the inner stars
   light up on hover, then (c) click the fourth star — redirecting the WebDriver
   to the next page is not an acceptable shortcut.
3. On the redirected page, change the **Policy** dropdown to `"Health Insurance"`.
4. Click **"Write a review"** and write random text (minimum 200 characters).
5. Press **submit**.
6. On success, a confirmation screen appears saying you have reviewed the
   institution. Then go to your profile and confirm the **review feed** updated.
7. Go to <https://wallethub.com/profile/> and assert that the review is visible.

> **Note:** Another tester should be able to pick up and continue this code with
> ease. Prefer efficient, maintainable, readable tests over existing
> wrapper/helper frameworks used as shortcuts.
