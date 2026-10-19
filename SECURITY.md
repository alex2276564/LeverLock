# Security Policy

**⚠️ IMPORTANT NOTE:**

This project follows **best-practice security**, but **cannot guarantee 100% protection** against zero-day exploits or highly targeted attacks.
For **enterprise-grade security requirements**, use **commercially supported solutions** with dedicated threat intelligence.

---

## Threat model

**Considered attack vectors** (prioritized by likelihood/risk):

1. **Command injection**
2. **Business logic bypass**
3. **Supply chain**
4. **Input sanitization bypass**
5. **Network attacks**
6. **Dependency updating**
7. **Text / formatting injection**

---

## Command injection

### Command framework

All plugin commands are registered through a single internal command framework shared across our plugins, rather than ad-hoc per-command string parsing. This framework provides the following protections by design:

- **Static, developer-defined command tree.** The set of valid commands, subcommands, and arguments is fully defined in code at plugin startup and cannot be mutated at runtime. User input matches only against explicitly registered subcommands; arbitrary tokens are treated strictly as literal arguments and cannot trigger hidden nodes (e.g., in `/<cmd> <group> op <player>`, `op` is parsed only as a literal player name argument, never as an internal `op` subcommand).
- **Typed, bounded argument parsing.** Arguments are strongly typed (e.g., string, integer, online player) and parsed before the command handler runs. Parsing automatically enforces null/empty checks, length limits, and strict format validation, rejecting malformed or oversized input before it reaches any business logic.
- **No dynamic dispatch or reflection.** Command execution always invokes a specific handler function bound to the matched command node at registration time. User input is never used to look up, construct, or invoke handlers by name or via reflection.
- **Per-node permission checks.** Every command node enforces its own permission string, checked against the sender before reaching argument parsing or handler execution.
- **No shell/OS execution.** Commands are dispatched through the Minecraft server's internal dispatcher, making classic shell metacharacters (`;`, `|`, `&`) completely inert. When building new console commands from templates (e.g., substituting player names), values undergo context-specific sanitization to strip characters that could construct unexpected multi-command sequences.

### Other injection

Beyond commands, all untrusted input from network events or external APIs follows the "sanitize at the boundary" principle. Raw values are never used directly for logging, display, or template substitution. Instead, they pass through a centralized utility with a context-specific sanitize type (e.g., name or network address) that applies strict allow-lists or escaping appropriate to the destination risk.

---

## Data sanitization

Whenever data comes from external or untrusted sources (user input, network responses, version tags, IP addresses, etc.), it is validated and sanitized before being used.

**Current protection:**

- Blocks **basic attacks**.

- May not fully prevent advanced bypasses (e.g., obfuscation).  
  *Such attacks are rare* and require deep technical knowledge to exploit.

**Limitations:**

- Filters prioritize **broad compatibility** (e.g., allow valid Unicode).

- **Admins should enforce whitelisting** for sensitive inputs (e.g., plugin for command restrictions).

**Command & argument handling:**

- Command arguments go through **lightweight validation** in the command framework (`ArgumentType`): checks for null/empty values, length limits, and basic format. They are not “sanitized” there, so that exact values (e.g., player names for `Bukkit.getPlayerExact`) remain usable as keys.

- When those values are later used at **trust boundaries** — logged, shown to players, injected into templates, or passed to external services — they are passed through `SecurityUtils` with a context-specific `SanitizeType` (e.g., `PLAYER_NAME`, `COMMAND`, `ERROR_MESSAGE`, `IP_ADDRESS`, etc.). Only these boundary points perform strong sanitization.

**Future plans:**

- Improvements to detect bypasses.

---

## Network Security

All external HTTP communications use TLS where possible.

The built-in `HttpUtils` wrapper enforces:

- **Timeouts**: Connection (5s), Request (10s).
- **Response limits**: Hard cap of **256 KiB** to prevent DoS via large payloads.
- **Typed error handling**: Each HTTP call returns a structured `HttpResponse`
  that exposes an explicit `HttpErrorType` (`TIMEOUT`, `CONNECTION_FAILED`,
  `RESPONSE_TOO_LARGE`, `INVALID_JSON`, etc.) instead of throwing checked
  exceptions or silently returning an empty JSON object on failure.
- **No implicit redirects**: `HttpUtils` never follows HTTP redirects
  (`Redirect.NEVER`), so requests cannot be transparently redirected to
  unexpected or untrusted endpoints.

---

## Symlinks in the data directory

By design, this plugin **follows symbolic links** inside its data folder for
configuration files and backups.

This allows advanced setups where configs are shared between multiple servers
(e.g. via Docker volumes or symlinked config files).

---

## Supply chain security

Every release is built via GitHub Actions with CI runner hardening enabled.
Each release artifact ships with a **SLSA Build Level 3** provenance file
and a **SHA-256 checksum** — both are published on the release page and can
be used to verify the integrity of the JAR.

### Binary artifacts in the repository

- `gradle-wrapper.jar`
  - **Location:** `gradle/wrapper/gradle-wrapper.jar`
  - **Reason it is committed:** Standard Gradle wrapper distribution mechanism, recommended by Gradle documentation.
  - **Risk mitigation:** The wrapper is pinned via `distributionSha256Sum` in `gradle-wrapper.properties`; Gradle verifies the downloaded distribution ZIP against this hash.

### Dependency locking

- Gradle dependencies
  - **Status:** **Locked**
  - **Details:** `dependencyLocking { lockAllConfigurations() }` is enabled, so all configurations are locked and upgrades are explicit.
- Gradle wrapper distribution
  - **Status:** **SHA-256 pinned**
  - **Details:** The Gradle wrapper is configured with `distributionSha256Sum` and `validateDistributionUrl=true` in `gradle-wrapper.properties`, so the Gradle distribution ZIP specified in `distributionUrl` is verified against a known SHA-256 hash and must come from `services.gradle.org`.
- GitHub Actions
  - **Status:** **SHA-pinned via lockfile**
  - **Details:** Workflow files reference actions by version tag (required for Renovate's `minimumReleaseAge` cooldown), but the commit actually executed on the runner is pinned via `.github/workflows/actions.lock`, maintained automatically by Renovate (`gh-actions-lock`).

---

## CI hardening

### Protections

- **StepSecurity Harden Runner** is enabled for all GitHub Actions workflows (except reusable workflows run in other repositories). It monitors outbound network and process activity on the runner at runtime and provides telemetry for suspicious behavior.
- **Socket “sfw” firewall wrapper** is used around some high-risk tooling (e.g., package managers) to add an additional inspection layer for supply‑chain risks.
- **StepSecurity hardened/drop-in Actions** (for example, replacements for popular third‑party Actions) are preferred where available, so that entire Actions are vetted and maintained by a security-focused vendor. However, these hardened variants may sometimes lag behind upstream releases, so regular upstream Actions are still used when the StepSecurity replacement is not yet updated.

### Network egress policy

Ideally, this project aims to enforce a strict egress policy using Harden Runner:

- High‑risk jobs (write permissions, secrets, CD/publishing) should use `egress-policy: block` with an explicit `allowed-endpoints` allowlist to limit data exfiltration if an Action or dependency is compromised.
- Low‑risk jobs (read‑only, no secrets) should use `egress-policy: audit`, because scanning and analysis tools may legitimately contact changing endpoints over time and hard blocking would add too much maintenance overhead.

In practice, not all jobs currently run in `block` mode: most jobs use `egress-policy: audit` today, and block mode may be enabled selectively for the most sensitive workflows as maintenance capacity allows.

---

## Security scanning

Security scans (SCA/SAST/IAST) covering the codebase, its dependencies, and the CI/GitHub Actions pipeline are run regularly: SCA/SAST checks are triggered automatically on every commit and on a daily schedule, while deeper IAST scans (using AI agents) are launched manually during major refactors or upon request.

---

## Dependency updating

Dependencies are kept up to date using [Renovate](https://github.com/renovatebot/renovate).

- **Regular (non-security) updates:**
  - Checked at least **weekly**.
  - A `minimumReleaseAge` of **3 days** is applied, so only versions that have been out for a while are adopted for normal updates.

- **Security-related updates:**
  - Processed **without artificial delay** — security patches are not held back by `minimumReleaseAge`.
  - Renovate security advisories and/or GitHub’s security alerts are handled as soon as possible once available.

---

## **Text / formatting injection**

- **MiniMessage**: `unparsed()` blocks dangerous tag injection from user input, preventing injection via `<click>`, `<hover>`, etc. Only trusted placeholders are parsed.

---

## Repository hardening

GitHub repository-level settings:

- **Force-push protection:** **Enabled**.
- **Secret Protection (push protection + secret scanning):** **Enabled**.
- **Dependabot security alerts:** **Not enabled**.
- **Immutable releases:** **Not enabled**.
- **Branch protection rules:** **Not enabled**.
- **Required signed commits/Required signed tags:** **Not enabled**.
- **Mandatory PR review:** **Not enabled**.

---

## Reporting a vulnerability

If you discover a security vulnerability, please use the
[Security tab](https://github.com/alex2276564/LeverLock/security/advisories) to report it privately.  
Do **not** disclose security vulnerabilities publicly before they have been addressed.
