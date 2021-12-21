# Contributing

Thanks for considering contributing to Maracas!
Hereafter, we give you some information on how to contribute to the project.
Specifically, we show you how we handle issues and pull requests, as well as the general guidelines we use when programming.

Please note that this project is accompanied by a [code of conduct](https://github.com/alien-tools/maracas/blob/main/CONTRIBUTING.md).
If your participate in this project, you agree to abide by its terms.

## Issues & Enhancements
We encourage you to report issues and enhancements as [issues](https://github.com/alien-tools/maracas/issues) in our GitHub repository.

### Bugs
When creating an issue associated with a bug, please ensure you specify the following aspects:
- Maracas **version** you are working with;
- **inputs** to the program (if any);
- **steps to reproduce** the issue;
- the **actual behaviour** (what you got);
- the **expected behaviour** (what you wanted to get), and;
- the **stack trace** if any.

### Enhancements
When creating an issue associated with an enhancement, please ensure you specify the following aspects:
- the **feature** you would like to have;
- why you **need** it, and;
- how do you imagine it should **work**.


## Pull Requests
You are more than welcome to directly contribute to the project!
If it is either a bug fix or a feature implementation, we encourage you to first contact the maintainers, so we all agree on the next steps and define a common design.
Once the intentions and design are clear you can proceed as follows:

1. Fork the repository.
2. Compile, build, and test the project with Maven.
3. Run the tests to verify that all of them are passing before you start working on the project.  
4. In case of adding a bug fix or feature, implement your code as well as the tests that verify this functionality (see [Guidelines](#guidelines)). Notice that tests and a Javadoc are mandatory in order to accept your contribution :blush:.
5. In case of adding a refactoring or documentation, implement your changes (see [Guidelines](#guidelines)). No tests are needed in this case.
6. Verify that after introducing your changes all tests are passing (including the ones you have introduced).
7. Commit and push your changes to your fork using an appropriate description (see [Commits](#commits)).
8. Once you are done, create a pull request explaining the content of the request and well as the motivation behind it.
9. Wait until one of the maintainers performs a code review of your pull request. If needed resolve all the discussion threads that might emerge between the both of you. Once all threads are resolved your pull request will be integrated into the repository!


## Commits
When committing to the repository, we encourage you to:
- commit **often**;
- keep **related changes** in the same commit;
- only commit **complete work**, and;
- commit **tested** code with all passing tests.

Regarding the formatting of the commit consider to:
- capitalize the first letter of the commit description;
- keep the message short (70 characters or less);
- use a verb as the first word of your message;
- adhere to the imperative form (e.g. `Fix bug` instead of `Fixing bug` or `Fixed bug`);
- describe the change you have made, and;
- reference the issue you want to close (if any). For that, include the following text at the end of your commit description: `; fix #<issue-number>`.


## Guidelines

### Javadoc
Every type, field, or method defined in Maracas must have a proper Javadoc.
In the case of a **type**, the Javadoc must have a short description and then a long description (if needed) separated by a blank line.
In the case of a **field**, provide a short description of the corresponding object.
In the case of a **method**, provide a description of what the method does, describe all parameters using the `@params` tag, the method return type (if any) using the `@return` tag, and all possible exceptions using the `@throws` tag.
We encourage you to use the `@see` and `@link` tags every time you refer to external, types, fields, or methods.


## Attribution

These contribution guidelines have been adapted from the Cubun project developed at TU/e.
