
* [Data engineering project template](#data-engineering-project-template)
    * [Run Data Pipeline](#run-data-pipeline)
        * [Run on codespaces](#run-on-codespaces)
        * [Run locally](#run-locally)
    * [Architecture and services in this template](#architecture-and-services-in-this-template)
    * [Using template](#using-template)
    * [AI Code Review](#ai-code-review)
        * [Setup](#setup)
        * [How It Works](#how-it-works)
        * [Cost Considerations](#cost-considerations)
    * [Writing pipelines](#writing-pipelines)
    * [(Optional) Advanced cloud setup](#optional-advanced-cloud-setup)
        * [Prerequisites:](#prerequisites)
        * [Tear down infra](#tear-down-infra)


# Data engineering project template

Detailed explanation can be found **[`in this post`](https://www.startdataengineering.com/post/data-engineering-projects-with-free-template/)**

## Run Data Pipeline

Code available at **[data_engineering_project_template](https://github.com/josephmachado/data_engineering_project_template/tree/main?tab=readme-ov-file#data-engineering-project-template)** repository.

### Run on codespaces

You can run this data pipeline using GitHub codespaces. Follow the instructions below.

1. Create codespaces by going to the **[data_engineering_project_template](https://github.com/josephmachado/data_engineering_project_template/tree/main?tab=readme-ov-file#data-engineering-project-template)** repository, cloning it(or click `Use this template` button) and then clicking on `Create codespaces on main` button.
2. Wait for codespaces to start, then in the terminal type `make up`.
3. Wait for `make up` to complete, and then wait for 30s (for Airflow to start).
4. After 30s go to the `ports` tab and click on the link exposing port `8080` to access Airflow UI (username and password is `airflow`).

![codespaces start](./assets/images/cs1.png)
![codespaces make up](./assets/images/cs2.png)
![codespaces open url](./assets/images/cs3.png)

### Run locally

To run locally, you need:

1. [git](https://git-scm.com/book/en/v2/Getting-Started-Installing-Git)
2. [Github account](https://github.com/)
3. [Docker](https://docs.docker.com/engine/install/) with at least 4GB of RAM and [Docker Compose](https://docs.docker.com/compose/install/) v1.27.0 or later

Clone the repo and run the following commands to start the data pipeline:

```bash
git clone https://github.com/josephmachado/data_engineering_project_template.git
cd data_engineering_project_template
make up
sleep 30 # wait for Airflow to start
make ci # run checks and tests
```
Go to [http:localhost:8080](http:localhost:8080) to see the Airflow UI. Username and password are both `airflow`.

## Architecture and services in this template

This data engineering project template, includes the following:

1. **`Airflow`**: To schedule and orchestrate DAGs.
2. **`Postgres`**: To store Airflow's details (which you can see via Airflow UI) and also has a schema to represent upstream databases.
3. **`DuckDB`**: To act as our warehouse
4. **`Quarto with Plotly`**: To convert code in `markdown` format to html files that can be embedded in your app or servered as is.
5. **`cuallee`**: To run data quality checks on the data we extracted from CoinCap API.
6. **`minio`**: To provide an S3 compatible open source storage system.

For simplicity services 1-5 of the above are installed and run in one container defined [here](./containers/airflow/Dockerfile).

![DET](./assets/images/det2.png)

The `coincap_elt` DAG in the [Airflow UI](http://localhost:8080) will look like the below image:

![DAG](./assets/images/dag.png)

You can see the rendered html at [./visualizations/dashboard.html](https://github.com/josephmachado/data_engineering_project_template/blob/main/visualization/dashboard.html).

The file structure of our repo is as shown below:

![File strucutre](./assets/images/fs.png)

## Using template

You can use this repo as a template and create your own, click on the `Use this template` button.

![Template](./assets/images/template.png)

## AI Code Review

**Status:** ✅ Fully operational

This repository includes an automated AI-powered code review workflow that runs on every pull request. The workflow connects to a **centralized AI review server** and provides intelligent feedback on:

- 🤖 **Code Quality**: AI-powered review using enterprise-grade models
- 🔍 **Static Analysis**: Linting with pylint and flake8
- 📊 **Complexity Metrics**: Cyclomatic complexity and maintainability index
- 🔒 **Security Scanning**: Vulnerability detection with Trivy
- 🐳 **Docker Best Practices**: Dockerfile validation
- ⚠️ **Code Smells**: Detection of debugging statements, TODOs, and anti-patterns

### Setup

**One-time setup required:**

1. **Add Access Token Secret:**
   - Go to your repository **Settings** → **Secrets and variables** → **Actions**
   - Click **New repository secret**
   - Name: `AI_REVIEW_ACCESS_TOKEN`
   - Value: Your centralized service access token (obtain from your admin team)

2. **Ready to use:**
   - ✅ Connected to centralized review service at: `http://ai-codereview-dev-alb-1334724727.us-east-1.elb.amazonaws.com`
   - ✅ API Endpoint: `POST /api/review/pr` (for PR reviews)
   - ✅ Automatically runs on all pull requests
   - ✅ Posts review summaries and findings as PR comments

📋 **Detailed setup instructions:** [.github/SETUP_INSTRUCTIONS.md](.github/SETUP_INSTRUCTIONS.md)

The AI code review workflow will automatically run on:
- New pull requests
- Updates to existing pull requests
- Reopened pull requests

**Configure review settings** (optional):
- Edit [.github/ai-review-config.yml](.github/ai-review-config.yml) to customize:
  - Review detail level
  - File patterns to include/exclude
  - Quality thresholds
  - Security check patterns

### How It Works

The AI code review workflow consists of **six jobs**:

1. **ai-code-review**: Sends code changes to centralized AI review server, which analyzes the code and returns review comments
2. **ai-code-quality-check**: Python-specific quality analysis with pylint and flake8
3. **python-code-complexity**: Analyzes cyclomatic complexity and maintainability metrics
4. **ai-security-scan**: Scans for security vulnerabilities and checks Docker best practices
5. **aggregate-review-results**: Combines all findings into comprehensive report and PR comment
6. **quality-gate**: 🚦 **Calculates quality score and blocks merge if score < 90**

### 🚦 Quality Gate Enforcement

Every PR receives a **quality score (0-100)** based on issues severity:
- 🚨 Critical: -20 points each
- ❌ Error: -10 points each
- ⚠️  Warning: -5 points each
- 💡 Info: -1 point each

**Minimum Score Required:** 90 / 100

**If score < 90:** ❌ Workflow fails, PR is blocked from merging until issues are addressed.

**Example scoring:**
- 0 critical, 0 errors, 2 warnings, 0 info = Score 90 ✅ (Just passing)
- 1 critical, 2 errors, 0 warnings, 0 info = Score 60 ❌ (Blocked)
- 0 critical, 0 errors, 0 warnings, 5 info = Score 95 ✅ (Excellent!)

📖 **Complete Guide:** [Quality Gate Documentation](.github/QUALITY_GATE_GUIDE.md)

### 📊 Review Reports & Analytics

Each PR receives:

- **📝 Comprehensive PR Comment** - Aggregated findings from all review tools with severity breakdown
- **📥 Downloadable Report** - Detailed markdown report available as workflow artifact (90 days retention)
- **📈 Analytics Dashboard** - Interactive dashboard tracking code quality trends at [`/analytics/review-dashboard.html`](analytics/README.md)

**What's included in reports:**
- Total issues by severity (Critical, Error, Warning, Info)
- Detailed issue list with file locations and line numbers
- Issues grouped by source (AI Review, Static Analysis, Complexity, Security)
- Executive summary and key metrics

**View the Analytics Dashboard:**
Navigate to [`/analytics`](analytics/README.md) folder to see code quality trends, issue patterns, and review metrics over time.

For detailed documentation, see [.github/workflows/README.md](.github/workflows/README.md).

### Centralized Service Benefits

- ✅ **No Cost**: No individual API keys or billing required
- ✅ **Consistent Reviews**: All teams use the same review standards
- ✅ **Managed Updates**: AI models and prompts managed centrally
- ✅ **Better Performance**: Optimized infrastructure for fast reviews
- ✅ **Enterprise Features**: Advanced security and compliance features
- ✅ **Comprehensive Reporting**: Aggregated findings from all review tools
- ✅ **Analytics & Metrics**: Track code quality trends over time

### 🔄 Managing Across Multiple Repositories

For organizations with multiple repositories, you can use **reusable workflows** to manage the AI code review system centrally:

- **Define once, use everywhere** - Single source of truth
- **Automatic updates** - Changes apply to all repositories
- **Repository-specific customization** - Override defaults per repo
- **Version control** - Pin to specific versions or use latest

📖 **Full guide:** [.github/MULTI_REPO_MANAGEMENT.md](.github/MULTI_REPO_MANAGEMENT.md)

**Quick example for other repos:**
```yaml
# .github/workflows/ai-code-review.yml
jobs:
  review:
    uses: your-org/github-workflows/.github/workflows/ai-code-review-reusable.yml@main
    secrets:
      ai-review-token: ${{ secrets.AI_REVIEW_ACCESS_TOKEN }}
```

## Writing pipelines

We have a sample pipeline at [coincap_elt.py](./dags/coincap_elt.py) that you can use as a starter to create your own DAGs. The tests are available at [./tests](./tests) folder.

Once the `coincap_elt` DAG runs, we can see the dashboard html at [./visualization/dashboard.html](./visualization/dashboard.html) and will look like ![Dashboard](./assets/images/dash.png).

## (Optional) Advanced cloud setup

If you want to run your code on an EC2 instance, with terraform, follow the steps below.

### Prerequisites:

1. [Terraform](https://learn.hashicorp.com/tutorials/terraform/install-cli) 
2. [AWS account](https://aws.amazon.com/) 
3. [AWS CLI installed](https://docs.aws.amazon.com/cli/latest/userguide/install-cliv2.html) and [configured](https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-configure.html)

You can create your GitHub repository based on this template by clicking on the `Use this template button in the **[data_engineering_project_template](https://github.com/josephmachado/data_engineering_project_template)** repository. Clone your repository and replace content in the following files

1. **[CODEOWNERS](https://github.com/josephmachado/data_engineering_project_template/blob/main/.github/CODEOWNERS)**: In this file change the user id from `@josephmachado` to your Github user id.
2. **[cd.yml](https://github.com/josephmachado/data_engineering_project_template/blob/main/.github/workflows/cd.yml)**: In this file change the `data_engineering_project_template` part of the `TARGET` parameter to your repository name.
3. **[variable.tf](https://github.com/josephmachado/data_engineering_project_template/blob/main/terraform/variable.tf)**: In this file change the default values for `alert_email_id` and `repo_url` variables with your email and [github repository url](https://www.theserverside.com/blog/Coffee-Talk-Java-News-Stories-and-Opinions/GitHub-URL-find-use-example) respectively.

Run the following commands in your project directory.

```shell
# Create AWS services with Terraform
make tf-init # Only needed on your first terraform run (or if you add new providers)
make infra-up # type in yes after verifying the changes TF will make

# Wait until the EC2 instance is initialized, you can check this via your AWS UI
# See "Status Check" on the EC2 console, it should be "2/2 checks passed" before proceeding
# Wait another 5 mins, Airflow takes a while to start up

make cloud-airflow # this command will forward Airflow port from EC2 to your machine and opens it in the browser
# the user name and password are both airflow

make cloud-metabase # this command will forward Metabase port from EC2 to your machine and opens it in the browser
# use https://github.com/josephmachado/data_engineering_project_template/blob/main/env file to connect to the warehouse from metabase
```

For the [continuous delivery](https://github.com/josephmachado/data_engineering_project_template/blob/main/.github/workflows/cd.yml) to work, set up the infrastructure with terraform, & defined the following repository secrets. You can set up the repository secrets by going to `Settings > Secrets > Actions > New repository secret`.

1. **`SERVER_SSH_KEY`**: We can get this by running `terraform -chdir=./terraform output -raw private_key` in the project directory and paste the entire content in a new Action secret called SERVER_SSH_KEY.
2. **`REMOTE_HOST`**: Get this by running `terraform -chdir=./terraform output -raw ec2_public_dns` in the project directory.
3. **`REMOTE_USER`**: The value for this is **ubuntu**.

### Tear down infra

After you are done, make sure to destroy your cloud infrastructure.

```shell
make down # Stop docker containers on your computer
make infra-down # type in yes after verifying the changes TF will make
```

