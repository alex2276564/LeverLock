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
6. **Text / formatting injection**

## Data sanitization

Whenever data comes from external or untrusted sources (user input, network responses, version tags, IP addresses, etc.), it is validated and sanitized before being used.

**Current protection:**

- Blocks **basic attacks**.

- May not fully prevent advanced bypasses (e.g., obfuscation).
  *Such attacks are rare* and require deep technical knowledge to exploit.

**Limitations:**

- Filters prioritize **broad compatibility** (e.g., allow valid Unicode).

- **Admins should enforce whitelisting** for sensitive inputs (e.g., plugin for command restrictions).

**Future plans:**

Improvements to detect bypasses.

## Network Security

All external HTTP communications use TLS where possible.

The built-in `HttpUtils` wrapper enforces:

- **Timeouts**: Connection (5s), Request (10s).
- **Response limits**: Hard cap of **256 KiB** to prevent DoS via large payloads.
- **Safe JSON parsing**: Fails securely (empty `JSONObject` on errors).

## Symlinks in the data directory

By design, LeverLock **follows symbolic links** inside its data folder for
configuration files and backups.

This allows advanced setups where configs are shared between multiple servers
(e.g. via Docker volumes or symlinked config files).

## Supply chain security

Every release is built via GitHub Actions with CI runner hardening enabled.
Each release artifact ships with a **SLSA Build Level 3** provenance file
and a **SHA-256 checksum** — both are published on the release page and can
be used to verify the integrity of the JAR.

## CI hardening

All CI jobs are protected by StepSecurity Harden Runner, which monitors
outbound network and process activity on the runner at runtime.

Third-party Actions are referenced by tag rather than commit SHA.
SHA pinning is intentionally not used — it only provides strong guarantees
when combined with manual review of every upstream commit, which this
single-developer project cannot sustain. Runtime monitoring via Harden Runner
is the primary supply-chain control instead.

## Security scanning

Security scans (SCA/SAST/IAST) covering the codebase, its dependencies, and the CI/GitHub Actions pipeline are run regularly: SCA/SAST checks are triggered automatically on every commit and on a daily schedule, while deeper IAST scans (using AI agents) are launched manually during major refactors or upon request.

## **Text / formatting injection**

- **MiniMessage**: `unparsed()` blocks dangerous tag injection from user input, preventing injection via `<click>`, `<hover>`, etc. Only trusted placeholders are parsed.

## Reporting a vulnerability

If you discover a security vulnerability, please use the
[Security tab](https://github.com/alex2276564/LeverLock/security/advisories) to report it privately.  
Do **not** disclose security vulnerabilities publicly before they have been addressed.
