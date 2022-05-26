# Maracas Experiments

## Content

- [Running the Experiment] (#running-the-experiment)

## Running the Experiment

### Configure GitHub system variable
To run the Github experiment you must first set your GitHub access token as a system variable.
To this aim, open a new terminal and type the following command to modify the list of variables:

```
> nano /etc/environment
```

Then, add the following system variable at the end of the file:

```
GITHUB_ACCESS_TOKEN=<your_access_token>
```

Restart your laptop and run the main class from your preferred IDE.