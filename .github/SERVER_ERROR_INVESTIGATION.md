# AI Code Review - Server Integration Status

> **✅ Update (Feb 24, 2026):**  
> Server is now responding successfully (HTTP 200)! The HTTP 500 error has been resolved.
> The service is returning responses, though it may not always include specific line comments.

## Current Status: ✅ Working

The centralized AI review service is now operational:
- ✅ Server responding with HTTP 200
- ✅ Workflow successfully sending requests
- ✅ Review summaries being posted to PRs
- ℹ️ Line-specific comments may vary based on review results

---

## Previous Issue (RESOLVED)

**Error:** HTTP 500 Internal Server Error from centralized AI review service

**Error Message:**
```json
{
  "error": "Batch review failed",
  "details": "Cannot read properties of undefined (reading 'split')"
}
```

**Resolution:** Server-side issue was fixed on Feb 24, 2026.

---

## What We Know

### ✅ Client-Side (Workflow) Status

The GitHub Actions workflow is **correctly configured**:

1. **Payload Structure:** Valid JSON with all required fields
   ```json
   {
     "repository": "ravikik/data-pipeline-template",
     "pr_number": 1,                    // ✅ Number type
     "pr_title": "Commit for AI Review",
     "pr_description": "",              // ✅ Empty string (not null)
     "base_branch": "develop",
     "head_branch": "feature/1.0",
     "author": "ravikik",
     "diff": "diff --git a/...",        // ✅ Full git diff
     "files": [...]                      // ✅ Array of file paths
   }
   ```

2. **Payload Size:** ~82KB with 10 files to review

3. **Request Headers:** Correctly set
   - `Content-Type: application/json`
   - `x-access-token: <token>`
   - `X-GitHub-Token: <token>`
   - `X-Repository: ravikik/data-pipeline-template`
   - `X-PR-Number: 1`

4. **Endpoint:** `POST /api/review/pr` ✅

5. **JSON Validation:** Payload passes `jq` validation ✅

### ❌ Server-Side Issue

The error `"Cannot read properties of undefined (reading 'split')"` is a **JavaScript runtime error** occurring on the server.

This indicates the server code is attempting:
```javascript
someField.split(...)
```
where `someField` is `undefined`.

---

## Possible Causes

### 1. Server Expects Different Field Names

**Current:** `pr_number`, `pr_title`, `pr_description`, etc.

**Server might expect:**
- Camel case: `prNumber`, `prTitle`, `prDescription`
- Different names: `pullRequestNumber`, `title`, `description`
- Nested structure: `{pr: {number: 1, title: "..."}}`

### 2. Server Looks for Additional Fields

The server might be trying to access fields we're not sending:
- `url` or `pr_url`
- `commit_sha` or `head_sha`
- `changed_files` (we send `files`)
- Nested metadata fields

### 3. Server Parses Data from Wrong Location

The server might be:
- Reading from query parameters instead of request body
- Expecting data in headers
- Looking for multipart/form-data instead of JSON

### 4. Server-Side Bug

The server code might have a bug where it:
- Doesn't check if fields exist before calling `.split()`
- Assumes certain fields are always populated
- Has incorrect null/undefined handling

---

## Information Needed from Server Team

### 🔍 Please Check Server-Side Logs

1. **Stack Trace:** Where exactly is `.split()` being called?
   ```
   Example:
   at parseFiles (server.js:123)
   at handleReview (server.js:456)
   ```

2. **Which Field is Undefined?**
   - Is it a field we're sending?
   - Is it a field we're NOT sending?
   - Is it a nested field?

3. **What is the Expected Payload Structure?**
   - Field names (exact casing)
   - Required fields
   - Optional fields
   - Data types expected
   - Any nested structures

4. **Request Received by Server:**
   - Can you see what data the server actually received?
   - Does it match what we're sending?
   - Are headers being read correctly?

---

## Request: API Documentation

Please provide:

1. **Official API Specification** for `POST /api/review/pr`
   - Request body schema
   - Required/optional fields
   - Field types
   - Example request

2. **Error Handling Documentation**
   - What does this specific error mean?
   - How to resolve it?

3. **Validation Requirements**
   - Are there field length limits?
   - Specific regex patterns?
   - Required format for certain fields?

---

## Suggested Server-Side Fixes

If this is a server-side bug, consider:

1. **Add Defensive Programming:**
   ```javascript
   // Bad - will crash if field is undefined
   const parts = field.split('/');
   
   // Good - safe handling
   const parts = (field || '').split('/');
   // or
   if (field && typeof field === 'string') {
     const parts = field.split('/');
   }
   ```

2. **Add Request Validation:**
   - Validate all required fields exist
   - Return 400 Bad Request with clear error message
   - Example: `{"error": "Missing required field: xyz"}`

3. **Improve Error Messages:**
   ```json
   {
     "error": "Batch review failed",
     "details": "Field 'files[0].path' is undefined - expected string"
   }
   ```

---

## Workflow Debug Output

**From Latest Run:**
```
📋 Complete payload structure:
{
  "repository": "ravikik/data-pipeline-template",
  "pr_number": 1,
  "pr_title": "Commit for AI Review",
  "pr_description": "",
  "base_branch": "develop",
  "head_branch": "feature/1.0",
  "author": "ravikik",
  "diff_length": [actual length],
  "diff_sample": "diff --git a/.github/AI_REVIEW_GUIDE.md...",
  "files": [10 file paths]
}
```

**Workflow File:** `.github/workflows/ai-code-review.yml`

**Endpoint:** `http://ai-codereview-dev-alb-1334724727.us-east-1.elb.amazonaws.com/api/review/pr`

---

## Next Steps

### For Repository Owners:

1. ✅ Verify `AI_REVIEW_ACCESS_TOKEN` is set correctly
2. ✅ Check workflow runs for complete payload structure
3. ✅ Share this document with server admin team
4. ⏳ Wait for server team to investigate logs
5. ⏳ Request API documentation if not available

### For Server Admin Team:

1. 🔍 Check server logs for this specific request
2. 🔍 Identify which field is undefined
3. 📋 Provide API specification document
4. 🛠️ Fix server-side bug OR
5. 📢 Document required payload changes

---

## Temporary Workaround

While investigating, you can disable the AI review step to unblock PRs:

**Option 1: Comment out the step**
```yaml
# In .github/workflows/ai-code-review.yml
# - name: Send to Centralized AI Review Server
#   if: steps.pr-info.outputs.has_changes == 'true'
#   env:
#     ...
```

**Option 2: Skip AI review for specific PRs**
Add `[skip ai-review]` to PR title:
```
feat: Add new feature [skip ai-review]
```

---

## Contact Information

**Server Admin Team:** [Provide contact method]

**Service URL:** `http://ai-codereview-dev-alb-1334724727.us-east-1.elb.amazonaws.com`

**Issue Date:** February 23, 2026

**Repository:** ravikik/data-pipeline-template

**PR Number:** #1

---

## Updates

| Date | Update |
|------|--------|
| Feb 23, 2026 | Initial issue identified - HTTP 500 with split() error |
| | Workflow sending correct JSON payload |
| | Waiting for server team investigation |

---

**Status:** 🔴 Blocked - Waiting for server team to investigate and provide API specification

**Priority:** Medium - Workflow continues with other checks, but AI review is unavailable
