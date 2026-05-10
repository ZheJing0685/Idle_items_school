<h2>:mag: Vulnerabilities of <code>school-backend:optimized</code></h2>

<details open="true"><summary>:package: Image Reference</strong> <code>school-backend:optimized</code></summary>
<table>
<tr><td>digest</td><td><code>sha256:6431b6f2c06d031b78207434e5a9887294a68aa5cba7abfb4b660399b6626ffb</code></td><tr><tr><td>vulnerabilities</td><td><img alt="critical: 4" src="https://img.shields.io/badge/critical-4-8b1924"/> <img alt="high: 31" src="https://img.shields.io/badge/high-31-e25d68"/> <img alt="medium: 45" src="https://img.shields.io/badge/medium-45-fbb552"/> <img alt="low: 13" src="https://img.shields.io/badge/low-13-fce1a9"/> <img alt="unspecified: 1" src="https://img.shields.io/badge/unspecified-1-lightgrey"/></td></tr>
<tr><td>platform</td><td>linux/amd64</td></tr>
<tr><td>size</td><td>118 MB</td></tr>
<tr><td>packages</td><td>156</td></tr>
</table>
</details></table>
</details>

<table>
<tr><td valign="top">
<details><summary><img alt="critical: 2" src="https://img.shields.io/badge/C-2-8b1924"/> <img alt="high: 12" src="https://img.shields.io/badge/H-12-e25d68"/> <img alt="medium: 5" src="https://img.shields.io/badge/M-5-fbb552"/> <img alt="low: 5" src="https://img.shields.io/badge/L-5-fce1a9"/> <!-- unspecified: 0 --><strong>org.apache.tomcat.embed/tomcat-embed-core</strong> <code>10.1.19</code> (maven)</summary>

<small><code>pkg:maven/org.apache.tomcat.embed/tomcat-embed-core@10.1.19</code></small><br/>
<a href="https://scout.docker.com/v/CVE-2025-24813?s=github&n=tomcat-embed-core&ns=org.apache.tomcat.embed&t=maven&vr=%3E%3D10.1.0-M1%2C%3C10.1.35"><img alt="critical 9.2: CVE--2025--24813" src="https://img.shields.io/badge/CVE--2025--24813-lightgrey?label=critical%209.2&labelColor=8b1924"/></a> <i>Path Equivalence: 'file.name' (Internal Dot)</i>

<table>
<tr><td>Affected range</td><td><code>>=10.1.0-M1<br/><10.1.35</code></td></tr>
<tr><td>Fixed version</td><td><code>10.1.35</code></td></tr>
<tr><td>CVSS Score</td><td><code>9.2</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:4.0/AV:N/AC:L/AT:P/PR:N/UI:N/VC:H/VI:H/VA:H/SC:N/SI:N/SA:N/E:A</code></td></tr>
<tr><td>EPSS Score</td><td><code>94.136%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>100th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

Path Equivalence: 'file.Name' (Internal Dot) leading to Remote Code Execution and/or Information disclosure and/or malicious content added to uploaded files via write enabled Default Servlet in Apache Tomcat.

This issue affects Apache Tomcat: from 11.0.0-M1 through 11.0.2, from 10.1.0-M1 through 10.1.34, from 9.0.0.M1 through 9.0.98. The following versions were EOL at the time the CVE was created but are known to be affected: 8.5.0 though 8.5.100. Other, older, EOL versions may also be affected.

If all of the following were true, a malicious user was able to view security sensitive files and/or inject content into those files:
- writes enabled for the default servlet (disabled by default)
- support for partial PUT (enabled by default)
- a target URL for security sensitive uploads that was a sub-directory of a target URL for public uploads
- attacker knowledge of the names of security sensitive files being uploaded
- the security sensitive files also being uploaded via partial PUT

If all of the following were true, a malicious user was able to perform remote code execution:
- writes enabled for the default servlet (disabled by default)
- support for partial PUT (enabled by default)
- application was using Tomcat's file based session persistence with the default storage location
- application included a library that may be leveraged in a deserialization attack

Users are recommended to upgrade to version 11.0.3, 10.1.35 or 9.0.99, which fixes the issue.

</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2026-29145?s=github&n=tomcat-embed-core&ns=org.apache.tomcat.embed&t=maven&vr=%3E%3D10.1.0-M7%2C%3C10.1.53"><img alt="critical 9.1: CVE--2026--29145" src="https://img.shields.io/badge/CVE--2026--29145-lightgrey?label=critical%209.1&labelColor=8b1924"/></a> <i>Improper Authentication</i>

<table>
<tr><td>Affected range</td><td><code>>=10.1.0-M7<br/><10.1.53</code></td></tr>
<tr><td>Fixed version</td><td><code>10.1.53</code></td></tr>
<tr><td>CVSS Score</td><td><code>9.1</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:H/A:N</code></td></tr>
<tr><td>EPSS Score</td><td><code>0.035%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>10th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

CLIENT_CERT authentication does not fail as expected for some scenarios when soft fail is disabled vulnerability in Apache Tomcat, Apache Tomcat Native.

This issue affects Apache Tomcat: from 11.0.0-M1 through 11.0.18, from 10.1.0-M7 through 10.1.52, from 9.0.83 through 9.0.115; Apache Tomcat Native: from 1.1.23 through 1.1.34, from 1.2.0 through 1.2.39, from 1.3.0 through 1.3.6, from 2.0.0 through 2.0.13.

Users are recommended to upgrade to version Tomcat Native 1.3.7 or 2.0.14 and Tomcat 11.0.20, 10.1.53 and 9.0.116, which fix the issue.

</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2025-48988?s=github&n=tomcat-embed-core&ns=org.apache.tomcat.embed&t=maven&vr=%3E%3D10.1.0-M1%2C%3C%3D10.1.41"><img alt="high 8.7: CVE--2025--48988" src="https://img.shields.io/badge/CVE--2025--48988-lightgrey?label=high%208.7&labelColor=e25d68"/></a> <i>Allocation of Resources Without Limits or Throttling</i>

<table>
<tr><td>Affected range</td><td><code>>=10.1.0-M1<br/><=10.1.41</code></td></tr>
<tr><td>Fixed version</td><td><code>10.1.42</code></td></tr>
<tr><td>CVSS Score</td><td><code>8.7</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:4.0/AV:N/AC:L/AT:N/PR:N/UI:N/VC:N/VI:N/VA:H/SC:N/SI:N/SA:N</code></td></tr>
<tr><td>EPSS Score</td><td><code>0.759%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>73rd percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

Allocation of Resources Without Limits or Throttling vulnerability in Apache Tomcat.

This issue affects Apache Tomcat: from 11.0.0-M1 through 11.0.7, from 10.1.0-M1 through 10.1.41, from 9.0.0.M1 through 9.0.105. The following versions were EOL at the time the CVE was created but are known to be affected: 8.5.0 though 8.5.100. Other, older, EOL versions may also be affected.

Users are recommended to upgrade to version 11.0.8, 10.1.42 or 9.0.106, which fix the issue.

</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2024-34750?s=github&n=tomcat-embed-core&ns=org.apache.tomcat.embed&t=maven&vr=%3E%3D10.1.0-M1%2C%3C10.1.25"><img alt="high 8.7: CVE--2024--34750" src="https://img.shields.io/badge/CVE--2024--34750-lightgrey?label=high%208.7&labelColor=e25d68"/></a> <i>Uncontrolled Resource Consumption</i>

<table>
<tr><td>Affected range</td><td><code>>=10.1.0-M1<br/><10.1.25</code></td></tr>
<tr><td>Fixed version</td><td><code>10.1.25</code></td></tr>
<tr><td>CVSS Score</td><td><code>8.7</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:4.0/AV:N/AC:L/AT:N/PR:N/UI:N/VC:N/VI:N/VA:H/SC:N/SI:N/SA:N</code></td></tr>
<tr><td>EPSS Score</td><td><code>21.539%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>96th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

Improper Handling of Exceptional Conditions, Uncontrolled Resource Consumption vulnerability in Apache Tomcat. When processing an HTTP/2 stream, Tomcat did not handle some cases of excessive HTTP headers correctly. This led to a miscounting of active HTTP/2 streams which in turn led to the use of an incorrect infinite timeout which allowed connections to remain open which should have been closed. 

This issue affects Apache Tomcat: from 11.0.0-M1 through 11.0.0-M20, from 10.1.0-M1 through 10.1.24, from 9.0.0-M1 through 9.0.89. The following versions were EOL at the time the CVE was created but are known to be affected: 8.5.0 though 8.5.100.

Users are recommended to upgrade to version 11.0.0-M21, 10.1.25 or 9.0.90, which fixes the issue.

</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2025-55752?s=github&n=tomcat-embed-core&ns=org.apache.tomcat.embed&t=maven&vr=%3E%3D10.1.0-M1%2C%3C10.1.45"><img alt="high 7.7: CVE--2025--55752" src="https://img.shields.io/badge/CVE--2025--55752-lightgrey?label=high%207.7&labelColor=e25d68"/></a> <i>Relative Path Traversal</i>

<table>
<tr><td>Affected range</td><td><code>>=10.1.0-M1<br/><10.1.45</code></td></tr>
<tr><td>Fixed version</td><td><code>10.1.45</code></td></tr>
<tr><td>CVSS Score</td><td><code>7.7</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:4.0/AV:N/AC:L/AT:P/PR:L/UI:N/VC:H/VI:H/VA:H/SC:N/SI:N/SA:N</code></td></tr>
<tr><td>EPSS Score</td><td><code>0.112%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>29th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

The fix for bug 60013 introduced a regression where the rewritten URL was normalized before it was decoded. This introduced the possibility that, for rewrite rules that rewrite query parameters to the URL, an attacker could manipulate the request URI to bypass security constraints including the protection for /WEB-INF/ and /META-INF/. If PUT requests were also enabled then malicious files could be uploaded leading to remote code execution. PUT requests are normally limited to trusted users and it is considered unlikely that PUT requests would be enabled in conjunction with a rewrite that manipulated the URI.



This issue affects Apache Tomcat: from 11.0.0-M1 through 11.0.10, from 10.1.0-M1 through 10.1.44, from 9.0.0.M11 through 9.0.108.

The following versions were EOL at the time the CVE was created but are  known to be affected: 8.5.6 though 8.5.100. Other, older, EOL versions may also be affected. Users are recommended to upgrade to version 11.0.11 or later, 10.1.45 or later or 9.0.109 or later, which fix the issue.

</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2026-34487?s=github&n=tomcat-embed-core&ns=org.apache.tomcat.embed&t=maven&vr=%3E%3D10.1.0-M1%2C%3C10.1.54"><img alt="high 7.5: CVE--2026--34487" src="https://img.shields.io/badge/CVE--2026--34487-lightgrey?label=high%207.5&labelColor=e25d68"/></a> <i>Insertion of Sensitive Information into Log File</i>

<table>
<tr><td>Affected range</td><td><code>>=10.1.0-M1<br/><10.1.54</code></td></tr>
<tr><td>Fixed version</td><td><code>10.1.54</code></td></tr>
<tr><td>CVSS Score</td><td><code>7.5</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:N/A:N</code></td></tr>
<tr><td>EPSS Score</td><td><code>0.082%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>24th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

Insertion of Sensitive Information into Log File vulnerability in the cloud membership for clustering component of Apache Tomcat exposed the Kubernetes bearer token.

This issue affects Apache Tomcat: from 11.0.0-M1 through 11.0.20, from 10.1.0-M1 through 10.1.53, from 9.0.13 through 9.0.116.

Users are recommended to upgrade to version 11.0.21, 10.1.54 or 9.0.117, which fix the issue.

</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2026-34483?s=github&n=tomcat-embed-core&ns=org.apache.tomcat.embed&t=maven&vr=%3E%3D10.1.0-M1%2C%3C10.1.54"><img alt="high 7.5: CVE--2026--34483" src="https://img.shields.io/badge/CVE--2026--34483-lightgrey?label=high%207.5&labelColor=e25d68"/></a> <i>Improper Encoding or Escaping of Output</i>

<table>
<tr><td>Affected range</td><td><code>>=10.1.0-M1<br/><10.1.54</code></td></tr>
<tr><td>Fixed version</td><td><code>10.1.54</code></td></tr>
<tr><td>CVSS Score</td><td><code>7.5</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:N/A:N</code></td></tr>
<tr><td>EPSS Score</td><td><code>0.082%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>24th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

Improper Encoding or Escaping of Output vulnerability in the JsonAccessLogValve component of Apache Tomcat.

This issue affects Apache Tomcat: from 11.0.0-M1 through 11.0.20, from 10.1.0-M1 through 10.1.53, from 9.0.40 through 9.0.116.

Users are recommended to upgrade to version 11.0.21, 10.1.54 or 9.0.117 , which fix the issue.

</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2026-24880?s=github&n=tomcat-embed-core&ns=org.apache.tomcat.embed&t=maven&vr=%3E%3D10.1.0-M1%2C%3C10.1.52"><img alt="high 7.5: CVE--2026--24880" src="https://img.shields.io/badge/CVE--2026--24880-lightgrey?label=high%207.5&labelColor=e25d68"/></a> <i>Inconsistent Interpretation of HTTP Requests ('HTTP Request/Response Smuggling')</i>

<table>
<tr><td>Affected range</td><td><code>>=10.1.0-M1<br/><10.1.52</code></td></tr>
<tr><td>Fixed version</td><td><code>10.1.52</code></td></tr>
<tr><td>CVSS Score</td><td><code>7.5</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:N/I:H/A:N</code></td></tr>
<tr><td>EPSS Score</td><td><code>0.215%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>44th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

Inconsistent Interpretation of HTTP Requests ('HTTP Request/Response Smuggling') vulnerability in Apache Tomcat via invalid chunk extension.

This issue affects Apache Tomcat: from 11.0.0-M1 through 11.0.18, from 10.1.0-M1 through 10.1.52, from 9.0.0.M1 through 9.0.115, from 8.5.0 through 8.5.100, from 7.0.0 through 7.0.109.
Other, unsupported versions may also be affected.

Users are recommended to upgrade to version 11.0.20, 10.1.52 or 9.0.116, which fix the issue.

</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2026-24734?s=github&n=tomcat-embed-core&ns=org.apache.tomcat.embed&t=maven&vr=%3E%3D10.1.0-M7%2C%3C10.1.52"><img alt="high 7.5: CVE--2026--24734" src="https://img.shields.io/badge/CVE--2026--24734-lightgrey?label=high%207.5&labelColor=e25d68"/></a> <i>Improper Input Validation</i>

<table>
<tr><td>Affected range</td><td><code>>=10.1.0-M7<br/><10.1.52</code></td></tr>
<tr><td>Fixed version</td><td><code>10.1.52</code></td></tr>
<tr><td>CVSS Score</td><td><code>7.5</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:N/I:H/A:N</code></td></tr>
<tr><td>EPSS Score</td><td><code>0.094%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>26th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

Improper Input Validation vulnerability in Apache Tomcat Native, Apache Tomcat.

When using an OCSP responder, Tomcat Native (and Tomcat's FFM port of the Tomcat Native code) did not complete verification or freshness checks on the OCSP response which could allow certificate revocation to be bypassed.

This issue affects Apache Tomcat Native:  from 1.3.0 through 1.3.4, from 2.0.0 through 2.0.11; Apache Tomcat: from 11.0.0-M1 through 11.0.17, from 10.1.0-M7 through 10.1.51, from 9.0.83 through 9.0.114.


The following versions were EOL at the time the CVE was created but are 
known to be affected: from 1.1.23 through 1.1.34, from 1.2.0 through 1.2.39. Older EOL versions are not affected.

Apache Tomcat Native users are recommended to upgrade to versions 1.3.5 or later or 2.0.12 or later, which fix the issue.

Apache Tomcat users are recommended to upgrade to versions 11.0.18 or later, 10.1.52 or later or 9.0.115 or later which fix the issue.

</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2025-53506?s=github&n=tomcat-embed-core&ns=org.apache.tomcat.embed&t=maven&vr=%3E%3D10.1.0-M1%2C%3C10.1.43"><img alt="high 7.5: CVE--2025--53506" src="https://img.shields.io/badge/CVE--2025--53506-lightgrey?label=high%207.5&labelColor=e25d68"/></a> <i>Uncontrolled Resource Consumption</i>

<table>
<tr><td>Affected range</td><td><code>>=10.1.0-M1<br/><10.1.43</code></td></tr>
<tr><td>Fixed version</td><td><code>10.1.43</code></td></tr>
<tr><td>CVSS Score</td><td><code>7.5</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:N/I:N/A:H</code></td></tr>
<tr><td>EPSS Score</td><td><code>1.247%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>79th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

Uncontrolled Resource Consumption vulnerability in Apache Tomcat if an HTTP/2 client did not acknowledge the initial settings frame that reduces the maximum permitted concurrent streams.

This issue affects Apache Tomcat: from 11.0.0-M1 through 11.0.8, from 10.1.0-M1 through 10.1.42, from 9.0.0.M1 through 9.0.106. The following versions were EOL at the time the CVE was created but are known to be affected: 8.5.0 through 8.5.100.

Users are recommended to upgrade to version 11.0.9, 10.1.43 or 9.0.107, which fix the issue.

</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2025-52520?s=github&n=tomcat-embed-core&ns=org.apache.tomcat.embed&t=maven&vr=%3E%3D10.1.0-M1%2C%3C10.1.43"><img alt="high 7.5: CVE--2025--52520" src="https://img.shields.io/badge/CVE--2025--52520-lightgrey?label=high%207.5&labelColor=e25d68"/></a> <i>Integer Overflow or Wraparound</i>

<table>
<tr><td>Affected range</td><td><code>>=10.1.0-M1<br/><10.1.43</code></td></tr>
<tr><td>Fixed version</td><td><code>10.1.43</code></td></tr>
<tr><td>CVSS Score</td><td><code>7.5</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:N/I:N/A:H</code></td></tr>
<tr><td>EPSS Score</td><td><code>0.683%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>72nd percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

For some unlikely configurations of multipart upload, an Integer Overflow vulnerability in Apache Tomcat could lead to a DoS via bypassing of size limits.

This issue affects Apache Tomcat: from 11.0.0-M1 through 11.0.8, from 10.1.0-M1 through 10.1.42, from 9.0.0.M1 through 9.0.106. The following versions were EOL at the time the CVE was created but are known to be affected: 8.5.0 through 8.5.100. Other, older, EOL versions may also be affected.

Users are recommended to upgrade to version 11.0.9, 10.1.43 or 9.0.107, which fix the issue.

</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2025-48989?s=github&n=tomcat-embed-core&ns=org.apache.tomcat.embed&t=maven&vr=%3E%3D10.1.0-M1%2C%3C10.1.44"><img alt="high 7.5: CVE--2025--48989" src="https://img.shields.io/badge/CVE--2025--48989-lightgrey?label=high%207.5&labelColor=e25d68"/></a> <i>Improper Resource Shutdown or Release</i>

<table>
<tr><td>Affected range</td><td><code>>=10.1.0-M1<br/><10.1.44</code></td></tr>
<tr><td>Fixed version</td><td><code>10.1.44</code></td></tr>
<tr><td>CVSS Score</td><td><code>7.5</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:N/I:N/A:H</code></td></tr>
<tr><td>EPSS Score</td><td><code>0.353%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>58th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

Improper Resource Shutdown or Release vulnerability in Apache Tomcat made Tomcat vulnerable to the made you reset attack.

This issue affects Apache Tomcat: from 11.0.0-M1 through 11.0.9, from 10.1.0-M1 through 10.1.43 and from 9.0.0.M1 through 9.0.107. Older, EOL versions may also be affected.

Users are recommended to upgrade to one of versions 11.0.10, 10.1.44 or 9.0.108 which fix the issue.

</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2024-56337?s=github&n=tomcat-embed-core&ns=org.apache.tomcat.embed&t=maven&vr=%3E%3D10.1.0-M1%2C%3C10.1.34"><img alt="high 7.2: CVE--2024--56337" src="https://img.shields.io/badge/CVE--2024--56337-lightgrey?label=high%207.2&labelColor=e25d68"/></a> <i>Time-of-check Time-of-use (TOCTOU) Race Condition</i>

<table>
<tr><td>Affected range</td><td><code>>=10.1.0-M1<br/><10.1.34</code></td></tr>
<tr><td>Fixed version</td><td><code>10.1.34</code></td></tr>
<tr><td>CVSS Score</td><td><code>7.2</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:4.0/AV:N/AC:L/AT:P/PR:N/UI:N/VC:H/VI:H/VA:H/SC:N/SI:N/SA:N/E:U</code></td></tr>
<tr><td>EPSS Score</td><td><code>10.166%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>93rd percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

Time-of-check Time-of-use (TOCTOU) Race Condition vulnerability in Apache Tomcat.

This issue affects Apache Tomcat: from 11.0.0-M1 through 11.0.1, from 10.1.0-M1 through 10.1.33, from 9.0.0.M1 through 9.0.97.

The mitigation for CVE-2024-50379 was incomplete.

Users running Tomcat on a case insensitive file system with the default servlet write enabled (readonly initialisation 
parameter set to the non-default value of false) may need additional configuration to fully mitigate CVE-2024-50379 depending on which version of Java they are using with Tomcat:
- running on Java 8 or Java 11: the system property sun.io.useCanonCaches must be explicitly set to false (it defaults to true)
- running on Java 17: the system property sun.io.useCanonCaches, if set, must be set to false (it defaults to false)
- running on Java 21 onwards: no further configuration is required (the system property and the problematic cache have been removed)

Tomcat 11.0.3, 10.1.35 and 9.0.99 onwards will include checks that sun.io.useCanonCaches is set appropriately before allowing the default servlet to be write enabled on a case insensitive file system. Tomcat will also set sun.io.useCanonCaches to false by default where it can.

</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2024-50379?s=github&n=tomcat-embed-core&ns=org.apache.tomcat.embed&t=maven&vr=%3E%3D10.1.0-M1%2C%3C10.1.34"><img alt="high 7.2: CVE--2024--50379" src="https://img.shields.io/badge/CVE--2024--50379-lightgrey?label=high%207.2&labelColor=e25d68"/></a> <i>Time-of-check Time-of-use (TOCTOU) Race Condition</i>

<table>
<tr><td>Affected range</td><td><code>>=10.1.0-M1<br/><10.1.34</code></td></tr>
<tr><td>Fixed version</td><td><code>10.1.34</code></td></tr>
<tr><td>CVSS Score</td><td><code>7.2</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:4.0/AV:N/AC:L/AT:P/PR:N/UI:N/VC:H/VI:H/VA:H/SC:N/SI:N/SA:N/E:U</code></td></tr>
<tr><td>EPSS Score</td><td><code>84.979%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>99th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

Time-of-check Time-of-use (TOCTOU) Race Condition vulnerability during JSP compilation in Apache Tomcat permits an RCE on case insensitive file systems when the default servlet is enabled for write (non-default configuration).

This issue affects Apache Tomcat: from 11.0.0-M1 through 11.0.1, from 10.1.0-M1 through 10.1.33, from 9.0.0.M1 through 9.0.97. The following versions were EOL at the time the CVE was created but are known to be affected: 8.5.0 though 8.5.100. Other, older, EOL versions may also be affected.

Users are recommended to upgrade to version 11.0.2, 10.1.34 or 9.0.98, which fixes the issue.

</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2026-25854?s=github&n=tomcat-embed-core&ns=org.apache.tomcat.embed&t=maven&vr=%3E%3D10.1.0-M1%2C%3C10.1.53"><img alt="medium 6.9: CVE--2026--25854" src="https://img.shields.io/badge/CVE--2026--25854-lightgrey?label=medium%206.9&labelColor=fbb552"/></a> <i>URL Redirection to Untrusted Site ('Open Redirect')</i>

<table>
<tr><td>Affected range</td><td><code>>=10.1.0-M1<br/><10.1.53</code></td></tr>
<tr><td>Fixed version</td><td><code>10.1.53</code></td></tr>
<tr><td>CVSS Score</td><td><code>6.9</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:4.0/AV:N/AC:L/AT:N/PR:N/UI:N/VC:N/VI:N/VA:N/SC:L/SI:N/SA:N</code></td></tr>
<tr><td>EPSS Score</td><td><code>0.026%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>7th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

Occasional URL redirection to untrusted Site ('Open Redirect') vulnerability in Apache Tomcat via the LoadBalancerDrainingValve.

This issue affects Apache Tomcat: from 11.0.0-M1 through 11.0.18, from 10.1.0-M1 through 10.1.52, from 9.0.0.M23 through 9.0.115, from 8.5.30 through 8.5.100.
Other, unsupported versions may also be affected

Users are recommended to upgrade to version 11.0.20, 10.1.53 or 9.0.116, which fix the issue.

</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2025-31650?s=github&n=tomcat-embed-core&ns=org.apache.tomcat.embed&t=maven&vr=%3E%3D10.1.10%2C%3C10.1.40"><img alt="medium 6.6: CVE--2025--31650" src="https://img.shields.io/badge/CVE--2025--31650-lightgrey?label=medium%206.6&labelColor=fbb552"/></a> <i>Incomplete Cleanup</i>

<table>
<tr><td>Affected range</td><td><code>>=10.1.10<br/><10.1.40</code></td></tr>
<tr><td>Fixed version</td><td><code>10.1.40</code></td></tr>
<tr><td>CVSS Score</td><td><code>6.6</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:4.0/AV:N/AC:L/AT:N/PR:N/UI:N/VC:N/VI:N/VA:H/SC:N/SI:N/SA:N/E:U</code></td></tr>
<tr><td>EPSS Score</td><td><code>9.547%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>93rd percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

Improper Input Validation vulnerability in Apache Tomcat. Incorrect error handling for some invalid HTTP priority headers resulted in incomplete clean-up of the failed request which created a memory leak. A large number of such requests could trigger an OutOfMemoryException resulting in a denial of service.

This issue affects Apache Tomcat: from 9.0.76 through 9.0.102, from 10.1.10 through 10.1.39, from 11.0.0-M2 through 11.0.5. The following versions were EOL at the time the CVE was created but are known to be affected: 8.5.90 though 8.5.100.

Users are recommended to upgrade to version 9.0.104, 10.1.40 or 11.0.6 which fix the issue.

</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2025-66614?s=github&n=tomcat-embed-core&ns=org.apache.tomcat.embed&t=maven&vr=%3E%3D10.1.0-M1%2C%3C10.1.50"><img alt="medium 6.3: CVE--2025--66614" src="https://img.shields.io/badge/CVE--2025--66614-lightgrey?label=medium%206.3&labelColor=fbb552"/></a> <i>Improper Input Validation</i>

<table>
<tr><td>Affected range</td><td><code>>=10.1.0-M1<br/><10.1.50</code></td></tr>
<tr><td>Fixed version</td><td><code>10.1.49</code></td></tr>
<tr><td>CVSS Score</td><td><code>6.3</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:4.0/AV:N/AC:L/AT:P/PR:N/UI:N/VC:L/VI:L/VA:N/SC:N/SI:N/SA:N</code></td></tr>
<tr><td>EPSS Score</td><td><code>0.051%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>16th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

Improper Input Validation vulnerability.

This issue affects Apache Tomcat: from 11.0.0-M1 through 11.0.14, from 10.1.0-M1 through 10.1.49, from 9.0.0-M1 through 9.0.112.

The following versions were EOL at the time the CVE was created but are known to be affected: 8.5.0 through 8.5.100. Older EOL versions are not affected. Tomcat did not validate that the host name provided via the SNI extension was the same as the host name provided in the HTTP host header field. If Tomcat was configured with more than one virtual host and the TLS configuration for one of those hosts did not require client certificate authentication but another one did, it was possible for a client to bypass the client certificate authentication by sending different host names in the SNI extension and the HTTP host header field.

The vulnerability only applies if client certificate authentication is only enforced at the Connector. It does not apply if client certificate authentication is enforced at the web application.

Users are recommended to upgrade to version 11.0.15 or later, 10.1.50 or later or 9.0.113 or later, which fix the issue.

</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2025-49125?s=github&n=tomcat-embed-core&ns=org.apache.tomcat.embed&t=maven&vr=%3E%3D10.1.0-M1%2C%3C%3D10.1.41"><img alt="medium 6.3: CVE--2025--49125" src="https://img.shields.io/badge/CVE--2025--49125-lightgrey?label=medium%206.3&labelColor=fbb552"/></a> <i>Authentication Bypass Using an Alternate Path or Channel</i>

<table>
<tr><td>Affected range</td><td><code>>=10.1.0-M1<br/><=10.1.41</code></td></tr>
<tr><td>Fixed version</td><td><code>10.1.42</code></td></tr>
<tr><td>CVSS Score</td><td><code>6.3</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:4.0/AV:N/AC:L/AT:P/PR:N/UI:N/VC:L/VI:L/VA:N/SC:N/SI:N/SA:N</code></td></tr>
<tr><td>EPSS Score</td><td><code>0.349%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>57th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

Authentication Bypass Using an Alternate Path or Channel vulnerability in Apache Tomcat.  When using PreResources or PostResources mounted other than at the root of the web application, it was possible to access those resources via an unexpected path. That path was likely not to be protected by the same security constraints as the expected path, allowing those security constraints to be bypassed.

This issue affects Apache Tomcat: from 11.0.0-M1 through 11.0.7, from 10.1.0-M1 through 10.1.41, from 9.0.0.M1 through 9.0.105. The following versions were EOL at the time the CVE was created but are known to be affected: 8.5.0 through 8.5.100. Other, older, EOL versions may also be affected.

Users are recommended to upgrade to version 11.0.8, 10.1.42 or 9.0.106, which fix the issue.

</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2025-49124?s=github&n=tomcat-embed-core&ns=org.apache.tomcat.embed&t=maven&vr=%3E%3D10.1.0%2C%3C10.1.42"><img alt="medium 4.8: CVE--2025--49124" src="https://img.shields.io/badge/CVE--2025--49124-lightgrey?label=medium%204.8&labelColor=fbb552"/></a> <i>Untrusted Search Path</i>

<table>
<tr><td>Affected range</td><td><code>>=10.1.0<br/><10.1.42</code></td></tr>
<tr><td>Fixed version</td><td><code>10.1.42</code></td></tr>
<tr><td>CVSS Score</td><td><code>4.8</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:4.0/AV:L/AC:L/AT:P/PR:N/UI:N/VC:H/VI:H/VA:H/SC:N/SI:N/SA:N/E:U</code></td></tr>
<tr><td>EPSS Score</td><td><code>0.237%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>47th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

Untrusted Search Path vulnerability in Apache Tomcat installer for Windows. During installation, the Tomcat installer for Windows used icacls.exe without specifying a full path.

This issue affects Apache Tomcat: from 11.0.0-M1 through 11.0.7, from 10.1.0 through 10.1.41, from 9.0.23 through 9.0.105.

Users are recommended to upgrade to version 11.0.8, 10.1.42 or 9.0.106, which fix the issue.

</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2026-24733?s=github&n=tomcat-embed-core&ns=org.apache.tomcat.embed&t=maven&vr=%3E%3D10.1.0-M1%2C%3C10.1.50"><img alt="low 2.7: CVE--2026--24733" src="https://img.shields.io/badge/CVE--2026--24733-lightgrey?label=low%202.7&labelColor=fce1a9"/></a> <i>Improper Input Validation</i>

<table>
<tr><td>Affected range</td><td><code>>=10.1.0-M1<br/><10.1.50</code></td></tr>
<tr><td>Fixed version</td><td><code>10.1.49</code></td></tr>
<tr><td>CVSS Score</td><td><code>2.7</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:4.0/AV:N/AC:L/AT:N/PR:N/UI:N/VC:L/VI:N/VA:N/SC:N/SI:N/SA:N/E:U</code></td></tr>
<tr><td>EPSS Score</td><td><code>0.159%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>36th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

Improper Input Validation vulnerability in Apache Tomcat.

Tomcat did not limit HTTP/0.9 requests to the GET method. If a security constraint was configured to allow HEAD requests to a URI but deny GET requests, the user could bypass that constraint on GET requests by sending a (specification invalid) HEAD request using HTTP/0.9.

This issue affects Apache Tomcat: from 11.0.0-M1 through 11.0.14, from 10.1.0-M1 through 10.1.49, from 9.0.0.M1 through 9.0.112.


Older, EOL versions are also affected.

Users are recommended to upgrade to version 11.0.15 or later, 10.1.50 or later or 9.0.113 or later, which fixes the issue.

</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2025-31651?s=github&n=tomcat-embed-core&ns=org.apache.tomcat.embed&t=maven&vr=%3E%3D10.1.10%2C%3C10.1.40"><img alt="low 2.7: CVE--2025--31651" src="https://img.shields.io/badge/CVE--2025--31651-lightgrey?label=low%202.7&labelColor=fce1a9"/></a> <i>Improper Encoding or Escaping of Output</i>

<table>
<tr><td>Affected range</td><td><code>>=10.1.10<br/><10.1.40</code></td></tr>
<tr><td>Fixed version</td><td><code>10.1.40</code></td></tr>
<tr><td>CVSS Score</td><td><code>2.7</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:4.0/AV:N/AC:L/AT:N/PR:N/UI:N/VC:N/VI:L/VA:N/SC:N/SI:N/SA:N/E:U</code></td></tr>
<tr><td>EPSS Score</td><td><code>0.370%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>59th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

Improper Neutralization of Escape, Meta, or Control Sequences vulnerability in Apache Tomcat. For a subset of unlikely rewrite rule configurations, it was possible for a specially crafted request to bypass some rewrite rules. If those rewrite rules effectively enforced security constraints, those constraints could be bypassed.

This issue affects Apache Tomcat: from 11.0.0-M1 through 11.0.5, from 10.1.0-M1 through 10.1.39, from 9.0.0.M1 through 9.0.102. The following versions were EOL at the time the CVE was created but are known to be affected: 8.5.0 though 8.5.100. Other, older, EOL versions may also be affected.

Users are recommended to upgrade to version 9.0.104, 10.1.40 or 11.0.6, which fix the issue.

</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2025-61795?s=github&n=tomcat-embed-core&ns=org.apache.tomcat.embed&t=maven&vr=%3E%3D10.1.0-M1%2C%3C10.1.47"><img alt="low 2.3: CVE--2025--61795" src="https://img.shields.io/badge/CVE--2025--61795-lightgrey?label=low%202.3&labelColor=fce1a9"/></a> <i>Improper Resource Shutdown or Release</i>

<table>
<tr><td>Affected range</td><td><code>>=10.1.0-M1<br/><10.1.47</code></td></tr>
<tr><td>Fixed version</td><td><code>10.1.47</code></td></tr>
<tr><td>CVSS Score</td><td><code>2.3</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:4.0/AV:N/AC:L/AT:P/PR:L/UI:N/VC:N/VI:N/VA:H/SC:N/SI:N/SA:N/E:U</code></td></tr>
<tr><td>EPSS Score</td><td><code>0.127%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>32nd percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

If an error occurred (including exceeding limits) during the processing of a multipart upload, temporary copies of the uploaded parts written to disc were not cleaned up immediately but left for the garbage collection process to delete. Depending on JVM settings, application memory usage and application load, it was possible that space for the temporary copies of uploaded parts would be filled faster than GC cleared it, leading to a DoS.

This issue affects Apache Tomcat: from 11.0.0-M1 through 11.0.11, from 10.1.0-M1 through 10.1.46, from 9.0.0.M1 through 9.0.109.

The following versions were EOL at the time the CVE was created but are 
known to be affected: 8.5.0 though 8.5.100. Other, older, EOL versions may also be affected.
Users are recommended to upgrade to version 11.0.12 or later, 10.1.47 or later or 9.0.110 or later which fixes the issue.

</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2025-55754?s=github&n=tomcat-embed-core&ns=org.apache.tomcat.embed&t=maven&vr=%3E%3D10.1.0-M1%2C%3C10.1.45"><img alt="low 2.1: CVE--2025--55754" src="https://img.shields.io/badge/CVE--2025--55754-lightgrey?label=low%202.1&labelColor=fce1a9"/></a> <i>Improper Neutralization of Escape, Meta, or Control Sequences</i>

<table>
<tr><td>Affected range</td><td><code>>=10.1.0-M1<br/><10.1.45</code></td></tr>
<tr><td>Fixed version</td><td><code>10.1.45</code></td></tr>
<tr><td>CVSS Score</td><td><code>2.1</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:4.0/AV:N/AC:L/AT:P/PR:N/UI:A/VC:L/VI:L/VA:L/SC:N/SI:N/SA:N</code></td></tr>
<tr><td>EPSS Score</td><td><code>0.127%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>31st percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

Tomcat did not escape ANSI escape sequences in log messages. If Tomcat was running in a console on a Windows operating system, and the console supported ANSI escape sequences, it was possible for an attacker to use a specially crafted URL to inject ANSI escape sequences to manipulate the console and the clipboard and attempt to trick an administrator into running an attacker controlled command. While no attack vector was found, it may have been possible to mount this attack on other operating systems.



This issue affects Apache Tomcat: from 11.0.0-M1 through 11.0.10, from 10.1.0-M1 through 10.1.44, from 9.0.40 through 9.0.108.

The following versions were EOL at the time the CVE was created but are 
known to be affected: 8.5.60 though 8.5.100. Other, older, EOL versions may also be affected.
Users are recommended to upgrade to version 11.0.11 or later, 10.1.45 or later or 9.0.109 or later, which fix the issue.

</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2025-46701?s=github&n=tomcat-embed-core&ns=org.apache.tomcat.embed&t=maven&vr=%3E%3D10.1.0-M1%2C%3C10.1.41"><img alt="low 1.7: CVE--2025--46701" src="https://img.shields.io/badge/CVE--2025--46701-lightgrey?label=low%201.7&labelColor=fce1a9"/></a> <i>Improper Handling of Case Sensitivity</i>

<table>
<tr><td>Affected range</td><td><code>>=10.1.0-M1<br/><10.1.41</code></td></tr>
<tr><td>Fixed version</td><td><code>10.1.41</code></td></tr>
<tr><td>CVSS Score</td><td><code>1.7</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:4.0/AV:N/AC:L/AT:P/PR:N/UI:N/VC:L/VI:L/VA:N/SC:N/SI:N/SA:N/E:U/U:Clear</code></td></tr>
<tr><td>EPSS Score</td><td><code>0.132%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>32nd percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

Improper Handling of Case Sensitivity vulnerability in Apache Tomcat's GCI servlet allows security constraint bypass of security constraints that apply to the pathInfo component of a URI mapped to the CGI servlet.

This issue affects Apache Tomcat: from 11.0.0-M1 through 11.0.6, from 10.1.0-M1 through 10.1.40, from 9.0.0.M1 through 9.0.104. The following versions were EOL at the time the CVE was created but are known to be affected: 8.5.0 though 8.5.100. Other, older, EOL versions may also be affected.

Users are recommended to upgrade to version 11.0.7, 10.1.41 or 9.0.105, which fixes the issue.

</blockquote>
</details>
</details></td></tr>

<tr><td valign="top">
<details><summary><img alt="critical: 2" src="https://img.shields.io/badge/C-2-8b1924"/> <img alt="high: 0" src="https://img.shields.io/badge/H-0-lightgrey"/> <img alt="medium: 0" src="https://img.shields.io/badge/M-0-lightgrey"/> <img alt="low: 0" src="https://img.shields.io/badge/L-0-lightgrey"/> <!-- unspecified: 0 --><strong>org.springframework.security/spring-security-web</strong> <code>6.2.3</code> (maven)</summary>

<small><code>pkg:maven/org.springframework.security/spring-security-web@6.2.3</code></small><br/>
<a href="https://scout.docker.com/v/CVE-2024-38821?s=github&n=spring-security-web&ns=org.springframework.security&t=maven&vr=%3E%3D6.2.0%2C%3C6.2.7"><img alt="critical 9.3: CVE--2024--38821" src="https://img.shields.io/badge/CVE--2024--38821-lightgrey?label=critical%209.3&labelColor=8b1924"/></a> <i>Improper Authorization</i>

<table>
<tr><td>Affected range</td><td><code>>=6.2.0<br/><6.2.7</code></td></tr>
<tr><td>Fixed version</td><td><code>6.2.7</code></td></tr>
<tr><td>CVSS Score</td><td><code>9.3</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:4.0/AV:N/AC:L/AT:N/PR:N/UI:N/VC:H/VI:H/VA:N/SC:N/SI:N/SA:N</code></td></tr>
<tr><td>EPSS Score</td><td><code>13.090%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>94th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

Spring WebFlux applications that have Spring Security authorization rules on static resources can be bypassed under certain circumstances.

For this to impact an application, all of the following must be true:

  *  It must be a WebFlux application
  *  It must be using Spring's static resources support
  *  It must have a non-permitAll authorization rule applied to the static resources support

</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2026-22732?s=github&n=spring-security-web&ns=org.springframework.security&t=maven&vr=%3E%3D6.0.0%2C%3C%3D6.3.10"><img alt="critical 9.1: CVE--2026--22732" src="https://img.shields.io/badge/CVE--2026--22732-lightgrey?label=critical%209.1&labelColor=8b1924"/></a> <i>Direct Request ('Forced Browsing')</i>

<table>
<tr><td>Affected range</td><td><code>>=6.0.0<br/><=6.3.10</code></td></tr>
<tr><td>Fixed version</td><td><strong>Not Fixed</strong></td></tr>
<tr><td>CVSS Score</td><td><code>9.1</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:H/A:N</code></td></tr>
<tr><td>EPSS Score</td><td><code>0.027%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>8th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

When applications specify HTTP response headers for servlet applications using Spring Security, there is the possibility that the HTTP Headers will not be written. 
This issue affects Spring Security: from 5.7.0 through 5.7.21, from 5.8.0 through 5.8.23, from 6.3.0 through 6.3.14, from 6.4.0 through 6.4.14, from 6.5.0 through 6.5.8, from 7.0.0 through 7.0.3.

</blockquote>
</details>
</details></td></tr>

<tr><td valign="top">
<details><summary><img alt="critical: 0" src="https://img.shields.io/badge/C-0-lightgrey"/> <img alt="high: 6" src="https://img.shields.io/badge/H-6-e25d68"/> <img alt="medium: 6" src="https://img.shields.io/badge/M-6-fbb552"/> <img alt="low: 0" src="https://img.shields.io/badge/L-0-lightgrey"/> <!-- unspecified: 0 --><strong>libpng</strong> <code>1.6.44-r0</code> (apk)</summary>

<small><code>pkg:apk/alpine/libpng@1.6.44-r0?os_name=alpine&os_version=3.19</code></small><br/>
<a href="https://scout.docker.com/v/CVE-2026-25646?s=alpine&n=libpng&ns=alpine&t=apk&osn=alpine&osv=3.19&vr=%3C%3D1.6.44-r0"><img alt="high : CVE--2026--25646" src="https://img.shields.io/badge/CVE--2026--25646-lightgrey?label=high%20&labelColor=e25d68"/></a> 

<table>
<tr><td>Affected range</td><td><code><=1.6.44-r0</code></td></tr>
<tr><td>Fixed version</td><td><strong>Not Fixed</strong></td></tr>
<tr><td>EPSS Score</td><td><code>0.081%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>24th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>



</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2026-33636?s=alpine&n=libpng&ns=alpine&t=apk&osn=alpine&osv=3.19&vr=%3C%3D1.6.44-r0"><img alt="high : CVE--2026--33636" src="https://img.shields.io/badge/CVE--2026--33636-lightgrey?label=high%20&labelColor=e25d68"/></a> 

<table>
<tr><td>Affected range</td><td><code><=1.6.44-r0</code></td></tr>
<tr><td>Fixed version</td><td><strong>Not Fixed</strong></td></tr>
<tr><td>EPSS Score</td><td><code>0.054%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>17th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>



</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2026-33416?s=alpine&n=libpng&ns=alpine&t=apk&osn=alpine&osv=3.19&vr=%3C%3D1.6.44-r0"><img alt="high : CVE--2026--33416" src="https://img.shields.io/badge/CVE--2026--33416-lightgrey?label=high%20&labelColor=e25d68"/></a> 

<table>
<tr><td>Affected range</td><td><code><=1.6.44-r0</code></td></tr>
<tr><td>Fixed version</td><td><strong>Not Fixed</strong></td></tr>
<tr><td>EPSS Score</td><td><code>0.055%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>17th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>



</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2025-66293?s=alpine&n=libpng&ns=alpine&t=apk&osn=alpine&osv=3.19&vr=%3C%3D1.6.44-r0"><img alt="high : CVE--2025--66293" src="https://img.shields.io/badge/CVE--2025--66293-lightgrey?label=high%20&labelColor=e25d68"/></a> 

<table>
<tr><td>Affected range</td><td><code><=1.6.44-r0</code></td></tr>
<tr><td>Fixed version</td><td><strong>Not Fixed</strong></td></tr>
<tr><td>EPSS Score</td><td><code>0.088%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>25th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>



</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2025-65018?s=alpine&n=libpng&ns=alpine&t=apk&osn=alpine&osv=3.19&vr=%3C%3D1.6.44-r0"><img alt="high : CVE--2025--65018" src="https://img.shields.io/badge/CVE--2025--65018-lightgrey?label=high%20&labelColor=e25d68"/></a> 

<table>
<tr><td>Affected range</td><td><code><=1.6.44-r0</code></td></tr>
<tr><td>Fixed version</td><td><strong>Not Fixed</strong></td></tr>
<tr><td>EPSS Score</td><td><code>0.065%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>20th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>



</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2025-64720?s=alpine&n=libpng&ns=alpine&t=apk&osn=alpine&osv=3.19&vr=%3C%3D1.6.44-r0"><img alt="high : CVE--2025--64720" src="https://img.shields.io/badge/CVE--2025--64720-lightgrey?label=high%20&labelColor=e25d68"/></a> 

<table>
<tr><td>Affected range</td><td><code><=1.6.44-r0</code></td></tr>
<tr><td>Fixed version</td><td><strong>Not Fixed</strong></td></tr>
<tr><td>EPSS Score</td><td><code>0.079%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>23rd percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>



</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2026-22801?s=alpine&n=libpng&ns=alpine&t=apk&osn=alpine&osv=3.19&vr=%3C%3D1.6.44-r0"><img alt="medium : CVE--2026--22801" src="https://img.shields.io/badge/CVE--2026--22801-lightgrey?label=medium%20&labelColor=fbb552"/></a> 

<table>
<tr><td>Affected range</td><td><code><=1.6.44-r0</code></td></tr>
<tr><td>Fixed version</td><td><strong>Not Fixed</strong></td></tr>
<tr><td>EPSS Score</td><td><code>0.018%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>5th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>



</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2026-22695?s=alpine&n=libpng&ns=alpine&t=apk&osn=alpine&osv=3.19&vr=%3C%3D1.6.44-r0"><img alt="medium : CVE--2026--22695" src="https://img.shields.io/badge/CVE--2026--22695-lightgrey?label=medium%20&labelColor=fbb552"/></a> 

<table>
<tr><td>Affected range</td><td><code><=1.6.44-r0</code></td></tr>
<tr><td>Fixed version</td><td><strong>Not Fixed</strong></td></tr>
<tr><td>EPSS Score</td><td><code>0.030%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>9th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>



</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2025-64506?s=alpine&n=libpng&ns=alpine&t=apk&osn=alpine&osv=3.19&vr=%3C%3D1.6.44-r0"><img alt="medium : CVE--2025--64506" src="https://img.shields.io/badge/CVE--2025--64506-lightgrey?label=medium%20&labelColor=fbb552"/></a> 

<table>
<tr><td>Affected range</td><td><code><=1.6.44-r0</code></td></tr>
<tr><td>Fixed version</td><td><strong>Not Fixed</strong></td></tr>
<tr><td>EPSS Score</td><td><code>0.020%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>6th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>



</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2025-64505?s=alpine&n=libpng&ns=alpine&t=apk&osn=alpine&osv=3.19&vr=%3C%3D1.6.44-r0"><img alt="medium : CVE--2025--64505" src="https://img.shields.io/badge/CVE--2025--64505-lightgrey?label=medium%20&labelColor=fbb552"/></a> 

<table>
<tr><td>Affected range</td><td><code><=1.6.44-r0</code></td></tr>
<tr><td>Fixed version</td><td><strong>Not Fixed</strong></td></tr>
<tr><td>EPSS Score</td><td><code>0.014%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>2nd percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>



</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2025-28164?s=alpine&n=libpng&ns=alpine&t=apk&osn=alpine&osv=3.19&vr=%3C%3D1.6.44-r0"><img alt="medium : CVE--2025--28164" src="https://img.shields.io/badge/CVE--2025--28164-lightgrey?label=medium%20&labelColor=fbb552"/></a> 

<table>
<tr><td>Affected range</td><td><code><=1.6.44-r0</code></td></tr>
<tr><td>Fixed version</td><td><strong>Not Fixed</strong></td></tr>
<tr><td>EPSS Score</td><td><code>0.018%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>5th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>



</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2025-28162?s=alpine&n=libpng&ns=alpine&t=apk&osn=alpine&osv=3.19&vr=%3C%3D1.6.44-r0"><img alt="medium : CVE--2025--28162" src="https://img.shields.io/badge/CVE--2025--28162-lightgrey?label=medium%20&labelColor=fbb552"/></a> 

<table>
<tr><td>Affected range</td><td><code><=1.6.44-r0</code></td></tr>
<tr><td>Fixed version</td><td><strong>Not Fixed</strong></td></tr>
<tr><td>EPSS Score</td><td><code>0.018%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>5th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>



</blockquote>
</details>
</details></td></tr>

<tr><td valign="top">
<details><summary><img alt="critical: 0" src="https://img.shields.io/badge/C-0-lightgrey"/> <img alt="high: 2" src="https://img.shields.io/badge/H-2-e25d68"/> <img alt="medium: 3" src="https://img.shields.io/badge/M-3-fbb552"/> <img alt="low: 1" src="https://img.shields.io/badge/L-1-fce1a9"/> <img alt="unspecified: 1" src="https://img.shields.io/badge/U-1-lightgrey"/><strong>org.springframework/spring-webmvc</strong> <code>6.1.5</code> (maven)</summary>

<small><code>pkg:maven/org.springframework/spring-webmvc@6.1.5</code></small><br/>
<a href="https://scout.docker.com/v/CVE-2024-38819?s=github&n=spring-webmvc&ns=org.springframework&t=maven&vr=%3E%3D6.1.0%2C%3C6.1.14"><img alt="high 7.5: CVE--2024--38819" src="https://img.shields.io/badge/CVE--2024--38819-lightgrey?label=high%207.5&labelColor=e25d68"/></a> <i>Improper Limitation of a Pathname to a Restricted Directory ('Path Traversal')</i>

<table>
<tr><td>Affected range</td><td><code>>=6.1.0<br/><6.1.14</code></td></tr>
<tr><td>Fixed version</td><td><code>6.1.14</code></td></tr>
<tr><td>CVSS Score</td><td><code>7.5</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:N/A:N</code></td></tr>
<tr><td>EPSS Score</td><td><code>92.057%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>100th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

Applications serving static resources through the functional web frameworks WebMvc.fn or WebFlux.fn are vulnerable to path traversal attacks. An attacker can craft malicious HTTP requests and obtain any file on the file system that is also accessible to the process in which the Spring application is running.

</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2024-38816?s=github&n=spring-webmvc&ns=org.springframework&t=maven&vr=%3E%3D6.1.0%2C%3C6.1.13"><img alt="high 7.5: CVE--2024--38816" src="https://img.shields.io/badge/CVE--2024--38816-lightgrey?label=high%207.5&labelColor=e25d68"/></a> <i>Improper Limitation of a Pathname to a Restricted Directory ('Path Traversal')</i>

<table>
<tr><td>Affected range</td><td><code>>=6.1.0<br/><6.1.13</code></td></tr>
<tr><td>Fixed version</td><td><code>6.1.13</code></td></tr>
<tr><td>CVSS Score</td><td><code>7.5</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:N/A:N</code></td></tr>
<tr><td>EPSS Score</td><td><code>93.928%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>100th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

Applications serving static resources through the functional web frameworks WebMvc.fn or WebFlux.fn are vulnerable to path traversal attacks. An attacker can craft malicious HTTP requests and obtain any file on the file system that is also accessible to the process in which the Spring application is running.

Specifically, an application is vulnerable when both of the following are true:

  *  the web application uses RouterFunctions to serve static resources
  *  resource handling is explicitly configured with a FileSystemResource location


However, malicious requests are blocked and rejected when any of the following is true:

  *  the  Spring Security HTTP Firewall https://docs.spring.io/spring-security/reference/servlet/exploits/firewall.html  is in use
  *  the application runs on Tomcat or Jetty

</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2026-22737?s=github&n=spring-webmvc&ns=org.springframework&t=maven&vr=%3E%3D6.0.0%2C%3C%3D6.1.21"><img alt="medium 5.9: CVE--2026--22737" src="https://img.shields.io/badge/CVE--2026--22737-lightgrey?label=medium%205.9&labelColor=fbb552"/></a> <i>Improper Limitation of a Pathname to a Restricted Directory ('Path Traversal')</i>

<table>
<tr><td>Affected range</td><td><code>>=6.0.0<br/><=6.1.21</code></td></tr>
<tr><td>Fixed version</td><td><strong>Not Fixed</strong></td></tr>
<tr><td>CVSS Score</td><td><code>5.9</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:3.1/AV:N/AC:H/PR:N/UI:N/S:U/C:H/I:N/A:N</code></td></tr>
<tr><td>EPSS Score</td><td><code>0.092%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>26th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

Use of Java scripting engine enabled (e.g. JRuby, Jython) template views in Spring MVC and Spring WebFlux applications can result in disclosure of content from files outside the configured locations for script template views. This issue affects Spring Framework: from 7.0.0 through 7.0.5, from 6.2.0 through 6.2.16, from 6.1.0 through 6.1.25, from 5.3.0 through 5.3.46.

</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2025-41242?s=github&n=spring-webmvc&ns=org.springframework&t=maven&vr=%3E%3D6.1.0%2C%3C%3D6.1.21"><img alt="medium 5.9: CVE--2025--41242" src="https://img.shields.io/badge/CVE--2025--41242-lightgrey?label=medium%205.9&labelColor=fbb552"/></a> <i>Improper Limitation of a Pathname to a Restricted Directory ('Path Traversal')</i>

<table>
<tr><td>Affected range</td><td><code>>=6.1.0<br/><=6.1.21</code></td></tr>
<tr><td>Fixed version</td><td><strong>Not Fixed</strong></td></tr>
<tr><td>CVSS Score</td><td><code>5.9</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:3.1/AV:N/AC:H/PR:N/UI:N/S:U/C:H/I:N/A:N</code></td></tr>
<tr><td>EPSS Score</td><td><code>6.593%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>91st percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

Spring Framework MVC applications can be vulnerable to a “Path Traversal Vulnerability” when deployed on a non-compliant Servlet container.

An application can be vulnerable when all the following are true:

  *  the application is deployed as a WAR or with an embedded Servlet container
  *  the Servlet container  does not reject suspicious sequences https://jakarta.ee/specifications/servlet/6.1/jakarta-servlet-spec-6.1.html#uri-path-canonicalization 
  *  the application  serves static resources https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-config/static-resources.html#page-title  with Spring resource handling


We have verified that applications deployed on Apache Tomcat or Eclipse Jetty are not vulnerable, as long as default security features are not disabled in the configuration. Because we cannot check exploits against all Servlet containers and configuration variants, we strongly recommend upgrading your application.

</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2026-22745?s=github&n=spring-webmvc&ns=org.springframework&t=maven&vr=%3E%3D6.1.0%2C%3C%3D6.1.26"><img alt="medium 5.3: CVE--2026--22745" src="https://img.shields.io/badge/CVE--2026--22745-lightgrey?label=medium%205.3&labelColor=fbb552"/></a> <i>Uncontrolled Resource Consumption</i>

<table>
<tr><td>Affected range</td><td><code>>=6.1.0<br/><=6.1.26</code></td></tr>
<tr><td>Fixed version</td><td><strong>Not Fixed</strong></td></tr>
<tr><td>CVSS Score</td><td><code>5.3</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:N/I:N/A:L</code></td></tr>
<tr><td>EPSS Score</td><td><code>0.057%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>18th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

Spring MVC and WebFlux applications are vulnerable to Denial of Service attacks when resolving static resources.


More precisely, an application can be vulnerable when all the following are true:

  *  the application is using Spring MVC or Spring WebFlux
  *  the application is serving static resources from the file system
  *  the application is running on a Windows platform


When all the conditions above are met, the attacker can send malicious requests that are slow to resolve and that can keep HTTP connections in use. This can cause a Denial of Service on the application.

</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2026-22735?s=github&n=spring-webmvc&ns=org.springframework&t=maven&vr=%3E%3D6.0.0%2C%3C%3D6.1.21"><img alt="low 2.6: CVE--2026--22735" src="https://img.shields.io/badge/CVE--2026--22735-lightgrey?label=low%202.6&labelColor=fce1a9"/></a> <i>Improper Locking</i>

<table>
<tr><td>Affected range</td><td><code>>=6.0.0<br/><=6.1.21</code></td></tr>
<tr><td>Fixed version</td><td><strong>Not Fixed</strong></td></tr>
<tr><td>CVSS Score</td><td><code>2.6</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:3.1/AV:N/AC:H/PR:L/UI:R/S:U/C:N/I:L/A:N</code></td></tr>
<tr><td>EPSS Score</td><td><code>0.092%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>26th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

Spring MVC and WebFlux applications are vulnerable to stream corruption when using Server-Sent Events (SSE). This issue affects Spring Foundation: from 7.0.0 through 7.0.5, from 6.2.0 through 6.2.16, from 6.1.0 through 6.1.25, from 5.3.0 through 5.3.46.

</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2026-22741?s=github&n=spring-webmvc&ns=org.springframework&t=maven&vr=%3E%3D6.1.0%2C%3C%3D6.1.26"><img alt="unspecified : CVE--2026--22741" src="https://img.shields.io/badge/CVE--2026--22741-lightgrey?label=unspecified%20&labelColor=lightgrey"/></a> <i>Use of Cache Containing Sensitive Information</i>

<table>
<tr><td>Affected range</td><td><code>>=6.1.0<br/><=6.1.26</code></td></tr>
<tr><td>Fixed version</td><td><strong>Not Fixed</strong></td></tr>
<tr><td>EPSS Score</td><td><code>0.065%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>20th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

Spring MVC and WebFlux applications are vulnerable to cache poisoning when resolving static resources.


More precisely, an application can be vulnerable when all the following are true:

  *  the application is using Spring MVC or Spring WebFlux
  *  the application is configuring the  resource chain support https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-config/static-resources.html#page-title  with caching enabled
  *  the application adds support for encoded resources resolution
  *  the resource cache must be empty when the attacker has access to the application


When all the conditions above are met, the attacker can send malicious requests and poison the resource cache with resources using the wrong encoding. This can cause a denial of service by breaking the front-end application for clients.

</blockquote>
</details>
</details></td></tr>

<tr><td valign="top">
<details><summary><img alt="critical: 0" src="https://img.shields.io/badge/C-0-lightgrey"/> <img alt="high: 1" src="https://img.shields.io/badge/H-1-e25d68"/> <img alt="medium: 9" src="https://img.shields.io/badge/M-9-fbb552"/> <img alt="low: 1" src="https://img.shields.io/badge/L-1-fce1a9"/> <!-- unspecified: 0 --><strong>curl</strong> <code>8.14.1-r2</code> (apk)</summary>

<small><code>pkg:apk/alpine/curl@8.14.1-r2?os_name=alpine&os_version=3.19</code></small><br/>
<a href="https://scout.docker.com/v/CVE-2026-3805?s=alpine&n=curl&ns=alpine&t=apk&osn=alpine&osv=3.19&vr=%3C%3D8.14.1-r2"><img alt="high : CVE--2026--3805" src="https://img.shields.io/badge/CVE--2026--3805-lightgrey?label=high%20&labelColor=e25d68"/></a> 

<table>
<tr><td>Affected range</td><td><code><=8.14.1-r2</code></td></tr>
<tr><td>Fixed version</td><td><strong>Not Fixed</strong></td></tr>
<tr><td>EPSS Score</td><td><code>0.029%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>8th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>



</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2026-3784?s=alpine&n=curl&ns=alpine&t=apk&osn=alpine&osv=3.19&vr=%3C%3D8.14.1-r2"><img alt="medium : CVE--2026--3784" src="https://img.shields.io/badge/CVE--2026--3784-lightgrey?label=medium%20&labelColor=fbb552"/></a> 

<table>
<tr><td>Affected range</td><td><code><=8.14.1-r2</code></td></tr>
<tr><td>Fixed version</td><td><strong>Not Fixed</strong></td></tr>
<tr><td>EPSS Score</td><td><code>0.022%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>6th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>



</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2026-1965?s=alpine&n=curl&ns=alpine&t=apk&osn=alpine&osv=3.19&vr=%3C%3D8.14.1-r2"><img alt="medium : CVE--2026--1965" src="https://img.shields.io/badge/CVE--2026--1965-lightgrey?label=medium%20&labelColor=fbb552"/></a> 

<table>
<tr><td>Affected range</td><td><code><=8.14.1-r2</code></td></tr>
<tr><td>Fixed version</td><td><strong>Not Fixed</strong></td></tr>
<tr><td>EPSS Score</td><td><code>0.062%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>19th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>



</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2025-14017?s=alpine&n=curl&ns=alpine&t=apk&osn=alpine&osv=3.19&vr=%3C%3D8.14.1-r2"><img alt="medium : CVE--2025--14017" src="https://img.shields.io/badge/CVE--2025--14017-lightgrey?label=medium%20&labelColor=fbb552"/></a> 

<table>
<tr><td>Affected range</td><td><code><=8.14.1-r2</code></td></tr>
<tr><td>Fixed version</td><td><strong>Not Fixed</strong></td></tr>
<tr><td>EPSS Score</td><td><code>0.007%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>1st percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>



</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2025-13034?s=alpine&n=curl&ns=alpine&t=apk&osn=alpine&osv=3.19&vr=%3C%3D8.14.1-r2"><img alt="medium : CVE--2025--13034" src="https://img.shields.io/badge/CVE--2025--13034-lightgrey?label=medium%20&labelColor=fbb552"/></a> 

<table>
<tr><td>Affected range</td><td><code><=8.14.1-r2</code></td></tr>
<tr><td>Fixed version</td><td><strong>Not Fixed</strong></td></tr>
<tr><td>EPSS Score</td><td><code>0.011%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>1st percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>



</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2026-3783?s=alpine&n=curl&ns=alpine&t=apk&osn=alpine&osv=3.19&vr=%3C%3D8.14.1-r2"><img alt="medium : CVE--2026--3783" src="https://img.shields.io/badge/CVE--2026--3783-lightgrey?label=medium%20&labelColor=fbb552"/></a> 

<table>
<tr><td>Affected range</td><td><code><=8.14.1-r2</code></td></tr>
<tr><td>Fixed version</td><td><strong>Not Fixed</strong></td></tr>
<tr><td>EPSS Score</td><td><code>0.023%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>7th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>



</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2025-15079?s=alpine&n=curl&ns=alpine&t=apk&osn=alpine&osv=3.19&vr=%3C%3D8.14.1-r2"><img alt="medium : CVE--2025--15079" src="https://img.shields.io/badge/CVE--2025--15079-lightgrey?label=medium%20&labelColor=fbb552"/></a> 

<table>
<tr><td>Affected range</td><td><code><=8.14.1-r2</code></td></tr>
<tr><td>Fixed version</td><td><strong>Not Fixed</strong></td></tr>
<tr><td>EPSS Score</td><td><code>0.035%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>10th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>



</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2025-14819?s=alpine&n=curl&ns=alpine&t=apk&osn=alpine&osv=3.19&vr=%3C%3D8.14.1-r2"><img alt="medium : CVE--2025--14819" src="https://img.shields.io/badge/CVE--2025--14819-lightgrey?label=medium%20&labelColor=fbb552"/></a> 

<table>
<tr><td>Affected range</td><td><code><=8.14.1-r2</code></td></tr>
<tr><td>Fixed version</td><td><strong>Not Fixed</strong></td></tr>
<tr><td>EPSS Score</td><td><code>0.045%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>14th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>



</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2025-14524?s=alpine&n=curl&ns=alpine&t=apk&osn=alpine&osv=3.19&vr=%3C%3D8.14.1-r2"><img alt="medium : CVE--2025--14524" src="https://img.shields.io/badge/CVE--2025--14524-lightgrey?label=medium%20&labelColor=fbb552"/></a> 

<table>
<tr><td>Affected range</td><td><code><=8.14.1-r2</code></td></tr>
<tr><td>Fixed version</td><td><strong>Not Fixed</strong></td></tr>
<tr><td>EPSS Score</td><td><code>0.026%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>7th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>



</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2025-10966?s=alpine&n=curl&ns=alpine&t=apk&osn=alpine&osv=3.19&vr=%3C%3D8.14.1-r2"><img alt="medium : CVE--2025--10966" src="https://img.shields.io/badge/CVE--2025--10966-lightgrey?label=medium%20&labelColor=fbb552"/></a> 

<table>
<tr><td>Affected range</td><td><code><=8.14.1-r2</code></td></tr>
<tr><td>Fixed version</td><td><strong>Not Fixed</strong></td></tr>
<tr><td>EPSS Score</td><td><code>0.026%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>7th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>



</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2025-15224?s=alpine&n=curl&ns=alpine&t=apk&osn=alpine&osv=3.19&vr=%3C%3D8.14.1-r2"><img alt="low : CVE--2025--15224" src="https://img.shields.io/badge/CVE--2025--15224-lightgrey?label=low%20&labelColor=fce1a9"/></a> 

<table>
<tr><td>Affected range</td><td><code><=8.14.1-r2</code></td></tr>
<tr><td>Fixed version</td><td><strong>Not Fixed</strong></td></tr>
<tr><td>EPSS Score</td><td><code>0.084%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>24th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>



</blockquote>
</details>
</details></td></tr>

<tr><td valign="top">
<details><summary><img alt="critical: 0" src="https://img.shields.io/badge/C-0-lightgrey"/> <img alt="high: 1" src="https://img.shields.io/badge/H-1-e25d68"/> <img alt="medium: 3" src="https://img.shields.io/badge/M-3-fbb552"/> <img alt="low: 0" src="https://img.shields.io/badge/L-0-lightgrey"/> <!-- unspecified: 0 --><strong>org.springframework/spring-web</strong> <code>6.1.5</code> (maven)</summary>

<small><code>pkg:maven/org.springframework/spring-web@6.1.5</code></small><br/>
<a href="https://scout.docker.com/v/CVE-2024-22262?s=github&n=spring-web&ns=org.springframework&t=maven&vr=%3E%3D6.1.0%2C%3C6.1.6"><img alt="high 8.1: CVE--2024--22262" src="https://img.shields.io/badge/CVE--2024--22262-lightgrey?label=high%208.1&labelColor=e25d68"/></a> <i>URL Redirection to Untrusted Site ('Open Redirect')</i>

<table>
<tr><td>Affected range</td><td><code>>=6.1.0<br/><6.1.6</code></td></tr>
<tr><td>Fixed version</td><td><code>6.1.6</code></td></tr>
<tr><td>CVSS Score</td><td><code>8.1</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:3.1/AV:N/AC:L/PR:N/UI:R/S:U/C:H/I:H/A:N</code></td></tr>
<tr><td>EPSS Score</td><td><code>12.634%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>94th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

Applications that use UriComponentsBuilder to parse an externally provided URL (e.g. through a query parameter) AND perform validation checks on the host of the parsed URL may be vulnerable to a  open redirect https://cwe.mitre.org/data/definitions/601.html  attack or to a SSRF attack if the URL is used after passing validation checks.

This is the same as  CVE-2024-22259 https://spring.io/security/cve-2024-22259  and  CVE-2024-22243 https://spring.io/security/cve-2024-22243 , but with different input.

</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2025-41234?s=github&n=spring-web&ns=org.springframework&t=maven&vr=%3E%3D6.1.0%2C%3C6.1.21"><img alt="medium 6.5: CVE--2025--41234" src="https://img.shields.io/badge/CVE--2025--41234-lightgrey?label=medium%206.5&labelColor=fbb552"/></a> <i>Improper Neutralization of CRLF Sequences in HTTP Headers ('HTTP Request/Response Splitting')</i>

<table>
<tr><td>Affected range</td><td><code>>=6.1.0<br/><6.1.21</code></td></tr>
<tr><td>Fixed version</td><td><code>6.1.21</code></td></tr>
<tr><td>CVSS Score</td><td><code>6.5</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:3.1/AV:N/AC:H/PR:L/UI:R/S:C/C:H/I:L/A:N</code></td></tr>
<tr><td>EPSS Score</td><td><code>0.294%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>53rd percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

### Description

In Spring Framework, versions 6.0.x as of 6.0.5, versions 6.1.x and 6.2.x, an application is vulnerable to a reflected file download (RFD) attack when it sets a “Content-Disposition” header with a non-ASCII charset, where the filename attribute is derived from user-supplied input.

Specifically, an application is vulnerable when all the following are true:

  -  The header is prepared with `org.springframework.http.ContentDisposition`.
  -  The filename is set via `ContentDisposition.Builder#filename(String, Charset)`.
  -  The value for the filename is derived from user-supplied input.
  -  The application does not sanitize the user-supplied input.
  -  The downloaded content of the response is injected with malicious commands by the attacker (see RFD paper reference for details).


An application is not vulnerable if any of the following is true:

  -  The application does not set a “Content-Disposition” response header.
  -  The header is not prepared with `org.springframework.http.ContentDisposition`.
  -  The filename is set via one of:  
     - `ContentDisposition.Builder#filename(String)`, or
     - `ContentDisposition.Builder#filename(String, ASCII)`
  -  The filename is not derived from user-supplied input.
  -  The filename is derived from user-supplied input but sanitized by the application.
  -  The attacker cannot inject malicious content in the downloaded content of the response.


### Affected Spring Products and VersionsSpring Framework

  -  6.2.0 - 6.2.7
  -  6.1.0 - 6.1.20
  -  6.0.5 - 6.0.28
  -  Older, unsupported versions are not affected


### Mitigation

Users of affected versions should upgrade to the corresponding fixed version.

| Affected version(s) | Fix version | Availability |
| - | - | - |
| 6.2.x | 6.2.8 | OSS |
| 6.1.x | 6.1.21 | OSS |
| 6.0.x | 6.0.29 | [Commercial](https://enterprise.spring.io/) |

No further mitigation steps are necessary.

</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2024-38820?s=github&n=spring-web&ns=org.springframework&t=maven&vr=%3E%3D6.1.0%2C%3C6.1.14"><img alt="medium 5.3: CVE--2024--38820" src="https://img.shields.io/badge/CVE--2024--38820-lightgrey?label=medium%205.3&labelColor=fbb552"/></a> <i>Improper Handling of Case Sensitivity</i>

<table>
<tr><td>Affected range</td><td><code>>=6.1.0<br/><6.1.14</code></td></tr>
<tr><td>Fixed version</td><td><code>6.1.14</code></td></tr>
<tr><td>CVSS Score</td><td><code>5.3</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:N/I:L/A:N</code></td></tr>
<tr><td>EPSS Score</td><td><code>1.514%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>81st percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

The fix for CVE-2022-22968 made disallowedFields patterns in DataBinder case insensitive. However, String.toLowerCase() has some Locale dependent exceptions that could potentially result in fields not protected as expected.

</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2024-38809?s=github&n=spring-web&ns=org.springframework&t=maven&vr=%3E%3D6.1.0%2C%3C6.1.12"><img alt="medium 5.3: CVE--2024--38809" src="https://img.shields.io/badge/CVE--2024--38809-lightgrey?label=medium%205.3&labelColor=fbb552"/></a> <i>Inefficient Regular Expression Complexity</i>

<table>
<tr><td>Affected range</td><td><code>>=6.1.0<br/><6.1.12</code></td></tr>
<tr><td>Fixed version</td><td><code>6.1.12</code></td></tr>
<tr><td>CVSS Score</td><td><code>5.3</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:N/I:N/A:L</code></td></tr>
<tr><td>EPSS Score</td><td><code>0.140%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>34th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

### Description
Applications that parse ETags from `If-Match` or `If-None-Match` request headers are vulnerable to DoS attack.

### Affected Spring Products and Versions
org.springframework:spring-web in versions 

6.1.0 through 6.1.11
6.0.0 through 6.0.22
5.3.0 through 5.3.37

Older, unsupported versions are also affected

### Mitigation
Users of affected versions should upgrade to the corresponding fixed version.
6.1.x -> 6.1.12
6.0.x -> 6.0.23
5.3.x -> 5.3.38
No other mitigation steps are necessary.

Users of older, unsupported versions could enforce a size limit on `If-Match` and `If-None-Match` headers, e.g. through a Filter.

</blockquote>
</details>
</details></td></tr>

<tr><td valign="top">
<details><summary><img alt="critical: 0" src="https://img.shields.io/badge/C-0-lightgrey"/> <img alt="high: 1" src="https://img.shields.io/badge/H-1-e25d68"/> <img alt="medium: 1" src="https://img.shields.io/badge/M-1-fbb552"/> <img alt="low: 0" src="https://img.shields.io/badge/L-0-lightgrey"/> <!-- unspecified: 0 --><strong>io.netty/netty-codec</strong> <code>4.1.107.Final</code> (maven)</summary>

<small><code>pkg:maven/io.netty/netty-codec@4.1.107.Final</code></small><br/>
<a href="https://scout.docker.com/v/CVE-2026-42583?s=github&n=netty-codec&ns=io.netty&t=maven&vr=%3C%3D4.1.132.Final"><img alt="high 7.5: CVE--2026--42583" src="https://img.shields.io/badge/CVE--2026--42583-lightgrey?label=high%207.5&labelColor=e25d68"/></a> <i>Uncontrolled Resource Consumption</i>

<table>
<tr><td>Affected range</td><td><code><=4.1.132.Final</code></td></tr>
<tr><td>Fixed version</td><td><code>4.1.133.Final</code></td></tr>
<tr><td>CVSS Score</td><td><code>7.5</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:N/I:N/A:H</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

### Summary
Lz4FrameDecoder allocates a ByteBuf of size `decompressedLength` (up to 32 MB per block) before LZ4 runs. A peer only needs a 21-byte header plus `compressedLength` payload bytes - 22 bytes if `compressedLength == 1` - to force that allocation.

### Details
io.netty.handler.codec.compression.Lz4FrameDecoder#decode
Header fields are trusted for sizing. On the compressed path, after `readableBytes >= compressedLength`, the decoder does `ctx.alloc().buffer(decompressedLength, decompressedLength)` then decompresses.

### PoC
The test below demonstrates how an attacker sending 22 bytes will force the server to allocate 32MB

```java
    @<!-- -->Test
    void test() throws Exception {
        EventLoopGroup workerGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
        try {
            AtomicReference<Throwable> serverError = new AtomicReference<>();
            CountDownLatch latch = new CountDownLatch(1);

            ServerBootstrap server = new ServerBootstrap()
                    .group(workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @<!-- -->Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                    .addLast(new Lz4FrameDecoder())
                                    .addLast(new ChannelInboundHandlerAdapter() {
                                        @<!-- -->Override
                                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                            if (cause instanceof DecoderException) {
                                                serverError.set(cause.getCause());
                                            } else {
                                                serverError.set(cause);
                                            }
                                            latch.countDown();
                                        }
                                    });
                        }
                    });

            ChannelFuture serverChannel = server.bind(0).sync();

            Bootstrap client = new Bootstrap()
                    .group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInboundHandlerAdapter() {
                        @<!-- -->Override
                        public void channelActive(ChannelHandlerContext ctx) {
                            ByteBuf buf = ctx.alloc().buffer(22, 22);
                            buf.writeLong(MAGIC_NUMBER);
                            buf.writeByte(BLOCK_TYPE_COMPRESSED | 0x0F);
                            buf.writeIntLE(1);
                            buf.writeIntLE(1 << 25);
                            buf.writeIntLE(0);
                            buf.writeByte(0);

                            ctx.writeAndFlush(buf);

                            ctx.fireChannelActive();
                        }
                    });

            ChannelFuture clientChannel = client.connect(serverChannel.channel().localAddress()).sync();

            assertTrue(latch.await(10, TimeUnit.SECONDS));

            assertInstanceOf(IndexOutOfBoundsException.class, serverError.get());

            clientChannel.channel().close();
            serverChannel.channel().close();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
```

### Impact
Untrusted senders without per-channel / aggregate limits can stress memory with many small requests.

</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2025-58057?s=github&n=netty-codec&ns=io.netty&t=maven&vr=%3C4.1.125.Final"><img alt="medium 6.9: CVE--2025--58057" src="https://img.shields.io/badge/CVE--2025--58057-lightgrey?label=medium%206.9&labelColor=fbb552"/></a> <i>Improper Handling of Highly Compressed Data (Data Amplification)</i>

<table>
<tr><td>Affected range</td><td><code><4.1.125.Final</code></td></tr>
<tr><td>Fixed version</td><td><code>4.1.125.Final</code></td></tr>
<tr><td>CVSS Score</td><td><code>6.9</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:4.0/AV:N/AC:L/AT:N/PR:N/UI:N/VC:N/VI:N/VA:L/SC:N/SI:N/SA:N</code></td></tr>
<tr><td>EPSS Score</td><td><code>0.062%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>19th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

### Summary

With specially crafted input, `BrotliDecoder` and some other decompressing decoders will allocate a large number of reachable byte buffers, which can lead to denial of service.

### Details

`BrotliDecoder.decompress` has no limit in how often it calls `pull`, decompressing data 64K bytes at a time. The buffers are saved in the output list, and remain reachable until OOM is hit. This is basically a zip bomb.

Tested on 4.1.118, but there were no changes to the decoder since.

### PoC

Run this test case with `-Xmx1G`:

```java
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;

import java.util.Base64;

public class T {
    public static void main(String[] args) {
        EmbeddedChannel channel = new EmbeddedChannel(new BrotliDecoder());
        channel.writeInbound(Unpooled.wrappedBuffer(Base64.getDecoder().decode("aPpxD1tETigSAGj6cQ8vRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROKBIAaPpxD1tETigSAGj6cQ9bRE4oEgBo+nEPW0ROMBIAEgIaHwBETlQQVFcXlgA=")));
    }
}
```

Error:

```
Exception in thread "main" java.lang.OutOfMemoryError: Cannot reserve 4194304 bytes of direct buffer memory (allocated: 1069580289, limit: 1073741824)
	at java.base/java.nio.Bits.reserveMemory(Bits.java:178)
	at java.base/java.nio.DirectByteBuffer.<init>(DirectByteBuffer.java:121)
	at java.base/java.nio.ByteBuffer.allocateDirect(ByteBuffer.java:332)
	at io.netty.buffer.PoolArena$DirectArena.allocateDirect(PoolArena.java:718)
	at io.netty.buffer.PoolArena$DirectArena.newChunk(PoolArena.java:693)
	at io.netty.buffer.PoolArena.allocateNormal(PoolArena.java:213)
	at io.netty.buffer.PoolArena.tcacheAllocateNormal(PoolArena.java:195)
	at io.netty.buffer.PoolArena.allocate(PoolArena.java:137)
	at io.netty.buffer.PoolArena.allocate(PoolArena.java:127)
	at io.netty.buffer.PooledByteBufAllocator.newDirectBuffer(PooledByteBufAllocator.java:403)
	at io.netty.buffer.AbstractByteBufAllocator.directBuffer(AbstractByteBufAllocator.java:188)
	at io.netty.buffer.AbstractByteBufAllocator.directBuffer(AbstractByteBufAllocator.java:179)
	at io.netty.buffer.AbstractByteBufAllocator.buffer(AbstractByteBufAllocator.java:116)
	at io.netty.handler.codec.compression.BrotliDecoder.pull(BrotliDecoder.java:70)
	at io.netty.handler.codec.compression.BrotliDecoder.decompress(BrotliDecoder.java:101)
	at io.netty.handler.codec.compression.BrotliDecoder.decode(BrotliDecoder.java:137)
	at io.netty.handler.codec.ByteToMessageDecoder.decodeRemovalReentryProtection(ByteToMessageDecoder.java:530)
	at io.netty.handler.codec.ByteToMessageDecoder.callDecode(ByteToMessageDecoder.java:469)
	at io.netty.handler.codec.ByteToMessageDecoder.channelRead(ByteToMessageDecoder.java:290)
	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:444)
	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420)
	at io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412)
	at io.netty.channel.DefaultChannelPipeline$HeadContext.channelRead(DefaultChannelPipeline.java:1357)
	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:440)
	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420)
	at io.netty.channel.DefaultChannelPipeline.fireChannelRead(DefaultChannelPipeline.java:868)
	at io.netty.channel.embedded.EmbeddedChannel.writeInbound(EmbeddedChannel.java:348)
	at io.netty.handler.codec.compression.T.main(T.java:11)
```

### Impact

DoS for anyone using `BrotliDecoder` on untrusted input.

</blockquote>
</details>
</details></td></tr>

<tr><td valign="top">
<details><summary><img alt="critical: 0" src="https://img.shields.io/badge/C-0-lightgrey"/> <img alt="high: 1" src="https://img.shields.io/badge/H-1-e25d68"/> <img alt="medium: 1" src="https://img.shields.io/badge/M-1-fbb552"/> <img alt="low: 0" src="https://img.shields.io/badge/L-0-lightgrey"/> <!-- unspecified: 0 --><strong>org.springframework/spring-core</strong> <code>6.1.5</code> (maven)</summary>

<small><code>pkg:maven/org.springframework/spring-core@6.1.5</code></small><br/>
<a href="https://scout.docker.com/v/CVE-2025-41249?s=github&n=spring-core&ns=org.springframework&t=maven&vr=%3E%3D6.0.0%2C%3C%3D6.1.22"><img alt="high 7.5: CVE--2025--41249" src="https://img.shields.io/badge/CVE--2025--41249-lightgrey?label=high%207.5&labelColor=e25d68"/></a> <i>Improper Authorization</i>

<table>
<tr><td>Affected range</td><td><code>>=6.0.0<br/><=6.1.22</code></td></tr>
<tr><td>Fixed version</td><td><strong>Not Fixed</strong></td></tr>
<tr><td>CVSS Score</td><td><code>7.5</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:N/A:N</code></td></tr>
<tr><td>EPSS Score</td><td><code>0.069%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>21st percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

The Spring Framework annotation detection mechanism may not correctly resolve annotations on methods within type hierarchies with a parameterized super type with unbounded generics. This can be an issue if such annotations are used for authorization decisions.

Your application may be affected by this if you are using Spring Security's @<!-- -->EnableMethodSecurity feature.

You are not affected by this if you are not using @<!-- -->EnableMethodSecurity or if you do not use security annotations on methods in generic superclasses or generic interfaces.

This CVE is published in conjunction with  CVE-2025-41248 https://spring.io/security/cve-2025-41248 .

</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2024-38827?s=gitlab&n=spring-core&ns=org.springframework&t=maven&vr=%3C6.1.14"><img alt="medium 4.8: CVE--2024--38827" src="https://img.shields.io/badge/CVE--2024--38827-lightgrey?label=medium%204.8&labelColor=fbb552"/></a> <i>OWASP Top Ten 2017 Category A9 - Using Components with Known Vulnerabilities</i>

<table>
<tr><td>Affected range</td><td><code><6.1.14</code></td></tr>
<tr><td>Fixed version</td><td><code>6.1.14</code></td></tr>
<tr><td>CVSS Score</td><td><code>4.8</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:3.1/AV:N/AC:H/PR:N/UI:N/S:U/C:L/I:L/A:N</code></td></tr>
<tr><td>EPSS Score</td><td><code>0.410%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>61st percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

The usage of String.toLowerCase() and String.toUpperCase() has some Locale dependent exceptions that could potentially result in authorization rules not working properly.

</blockquote>
</details>
</details></td></tr>

<tr><td valign="top">
<details><summary><img alt="critical: 0" src="https://img.shields.io/badge/C-0-lightgrey"/> <img alt="high: 1" src="https://img.shields.io/badge/H-1-e25d68"/> <img alt="medium: 1" src="https://img.shields.io/badge/M-1-fbb552"/> <img alt="low: 0" src="https://img.shields.io/badge/L-0-lightgrey"/> <!-- unspecified: 0 --><strong>nghttp2</strong> <code>1.58.0-r0</code> (apk)</summary>

<small><code>pkg:apk/alpine/nghttp2@1.58.0-r0?os_name=alpine&os_version=3.19</code></small><br/>
<a href="https://scout.docker.com/v/CVE-2026-27135?s=alpine&n=nghttp2&ns=alpine&t=apk&osn=alpine&osv=3.19&vr=%3C%3D1.58.0-r0"><img alt="high : CVE--2026--27135" src="https://img.shields.io/badge/CVE--2026--27135-lightgrey?label=high%20&labelColor=e25d68"/></a> 

<table>
<tr><td>Affected range</td><td><code><=1.58.0-r0</code></td></tr>
<tr><td>Fixed version</td><td><strong>Not Fixed</strong></td></tr>
<tr><td>EPSS Score</td><td><code>0.024%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>7th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>



</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2024-28182?s=alpine&n=nghttp2&ns=alpine&t=apk&osn=alpine&osv=3.19&vr=%3C%3D1.58.0-r0"><img alt="medium : CVE--2024--28182" src="https://img.shields.io/badge/CVE--2024--28182-lightgrey?label=medium%20&labelColor=fbb552"/></a> 

<table>
<tr><td>Affected range</td><td><code><=1.58.0-r0</code></td></tr>
<tr><td>Fixed version</td><td><strong>Not Fixed</strong></td></tr>
<tr><td>EPSS Score</td><td><code>24.971%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>96th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>



</blockquote>
</details>
</details></td></tr>

<tr><td valign="top">
<details><summary><img alt="critical: 0" src="https://img.shields.io/badge/C-0-lightgrey"/> <img alt="high: 1" src="https://img.shields.io/badge/H-1-e25d68"/> <img alt="medium: 1" src="https://img.shields.io/badge/M-1-fbb552"/> <img alt="low: 0" src="https://img.shields.io/badge/L-0-lightgrey"/> <!-- unspecified: 0 --><strong>musl</strong> <code>1.2.4_git20230717-r5</code> (apk)</summary>

<small><code>pkg:apk/alpine/musl@1.2.4_git20230717-r5?os_name=alpine&os_version=3.19</code></small><br/>
<a href="https://scout.docker.com/v/CVE-2026-40200?s=alpine&n=musl&ns=alpine&t=apk&osn=alpine&osv=3.19&vr=%3C1.2.4_git20230717-r6"><img alt="high : CVE--2026--40200" src="https://img.shields.io/badge/CVE--2026--40200-lightgrey?label=high%20&labelColor=e25d68"/></a> 

<table>
<tr><td>Affected range</td><td><code><1.2.4_git20230717-r6</code></td></tr>
<tr><td>Fixed version</td><td><code>1.2.4_git20230717-r6</code></td></tr>
<tr><td>EPSS Score</td><td><code>0.018%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>5th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>



</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2026-6042?s=alpine&n=musl&ns=alpine&t=apk&osn=alpine&osv=3.19&vr=%3C1.2.4_git20230717-r6"><img alt="medium : CVE--2026--6042" src="https://img.shields.io/badge/CVE--2026--6042-lightgrey?label=medium%20&labelColor=fbb552"/></a> 

<table>
<tr><td>Affected range</td><td><code><1.2.4_git20230717-r6</code></td></tr>
<tr><td>Fixed version</td><td><code>1.2.4_git20230717-r6</code></td></tr>
<tr><td>EPSS Score</td><td><code>0.014%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>3rd percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>



</blockquote>
</details>
</details></td></tr>

<tr><td valign="top">
<details><summary><img alt="critical: 0" src="https://img.shields.io/badge/C-0-lightgrey"/> <img alt="high: 1" src="https://img.shields.io/badge/H-1-e25d68"/> <img alt="medium: 0" src="https://img.shields.io/badge/M-0-lightgrey"/> <img alt="low: 0" src="https://img.shields.io/badge/L-0-lightgrey"/> <!-- unspecified: 0 --><strong>io.netty/netty-handler</strong> <code>4.1.107.Final</code> (maven)</summary>

<small><code>pkg:maven/io.netty/netty-handler@4.1.107.Final</code></small><br/>
<a href="https://scout.docker.com/v/CVE-2025-24970?s=github&n=netty-handler&ns=io.netty&t=maven&vr=%3E%3D4.1.91.Final%2C%3C%3D4.1.117.Final"><img alt="high 7.5: CVE--2025--24970" src="https://img.shields.io/badge/CVE--2025--24970-lightgrey?label=high%207.5&labelColor=e25d68"/></a> <i>Improper Input Validation</i>

<table>
<tr><td>Affected range</td><td><code>>=4.1.91.Final<br/><=4.1.117.Final</code></td></tr>
<tr><td>Fixed version</td><td><code>4.1.118.Final</code></td></tr>
<tr><td>CVSS Score</td><td><code>7.5</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:N/I:N/A:H</code></td></tr>
<tr><td>EPSS Score</td><td><code>0.953%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>77th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

### Impact
When a special crafted packet is received via SslHandler it doesn't correctly handle validation of such a packet in all cases which can lead to a native crash.

### Workarounds
As workaround its possible to either disable the usage of the native SSLEngine or changing the code from:

```
SslContext context = ...;
SslHandler handler = context.newHandler(....);
```

to:

```
SslContext context = ...;
SSLEngine engine = context.newEngine(....);
SslHandler handler = new SslHandler(engine, ....);
```

</blockquote>
</details>
</details></td></tr>

<tr><td valign="top">
<details><summary><img alt="critical: 0" src="https://img.shields.io/badge/C-0-lightgrey"/> <img alt="high: 1" src="https://img.shields.io/badge/H-1-e25d68"/> <img alt="medium: 0" src="https://img.shields.io/badge/M-0-lightgrey"/> <img alt="low: 0" src="https://img.shields.io/badge/L-0-lightgrey"/> <!-- unspecified: 0 --><strong>openssl</strong> <code>3.1.8-r1</code> (apk)</summary>

<small><code>pkg:apk/alpine/openssl@3.1.8-r1?os_name=alpine&os_version=3.19</code></small><br/>
<a href="https://scout.docker.com/v/CVE-2025-15467?s=alpine&n=openssl&ns=alpine&t=apk&osn=alpine&osv=3.19&vr=%3C%3D3.1.8-r1"><img alt="high : CVE--2025--15467" src="https://img.shields.io/badge/CVE--2025--15467-lightgrey?label=high%20&labelColor=e25d68"/></a> 

<table>
<tr><td>Affected range</td><td><code><=3.1.8-r1</code></td></tr>
<tr><td>Fixed version</td><td><strong>Not Fixed</strong></td></tr>
<tr><td>EPSS Score</td><td><code>2.099%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>84th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>



</blockquote>
</details>
</details></td></tr>

<tr><td valign="top">
<details><summary><img alt="critical: 0" src="https://img.shields.io/badge/C-0-lightgrey"/> <img alt="high: 1" src="https://img.shields.io/badge/H-1-e25d68"/> <img alt="medium: 0" src="https://img.shields.io/badge/M-0-lightgrey"/> <img alt="low: 0" src="https://img.shields.io/badge/L-0-lightgrey"/> <!-- unspecified: 0 --><strong>org.springframework.boot/spring-boot</strong> <code>3.2.4</code> (maven)</summary>

<small><code>pkg:maven/org.springframework.boot/spring-boot@3.2.4</code></small><br/>
<a href="https://scout.docker.com/v/CVE-2025-22235?s=github&n=spring-boot&ns=org.springframework.boot&t=maven&vr=%3E%3D3.2.0%2C%3C%3D3.2.13.2"><img alt="high 7.3: CVE--2025--22235" src="https://img.shields.io/badge/CVE--2025--22235-lightgrey?label=high%207.3&labelColor=e25d68"/></a> <i>Improper Input Validation</i>

<table>
<tr><td>Affected range</td><td><code>>=3.2.0<br/><=3.2.13.2</code></td></tr>
<tr><td>Fixed version</td><td><strong>Not Fixed</strong></td></tr>
<tr><td>CVSS Score</td><td><code>7.3</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:L/I:L/A:L</code></td></tr>
<tr><td>EPSS Score</td><td><code>0.390%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>60th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

EndpointRequest.to() creates a matcher for null/** if the actuator endpoint, for which the EndpointRequest has been created, is disabled or not exposed.

Your application may be affected by this if all the following conditions are met:

  *  You use Spring Security
  *  EndpointRequest.to() has been used in a Spring Security chain configuration
  *  The endpoint which EndpointRequest references is disabled or not exposed via web
  *  Your application handles requests to /null and this path needs protection


You are not affected if any of the following is true:

  *  You don't use Spring Security
  *  You don't use EndpointRequest.to()
  *  The endpoint which EndpointRequest.to() refers to is enabled and is exposed
  *  Your application does not handle requests to /null or this path does not need protection

</blockquote>
</details>
</details></td></tr>

<tr><td valign="top">
<details><summary><img alt="critical: 0" src="https://img.shields.io/badge/C-0-lightgrey"/> <img alt="high: 1" src="https://img.shields.io/badge/H-1-e25d68"/> <img alt="medium: 0" src="https://img.shields.io/badge/M-0-lightgrey"/> <img alt="low: 0" src="https://img.shields.io/badge/L-0-lightgrey"/> <!-- unspecified: 0 --><strong>org.springframework.security/spring-security-crypto</strong> <code>6.2.3</code> (maven)</summary>

<small><code>pkg:maven/org.springframework.security/spring-security-crypto@6.2.3</code></small><br/>
<a href="https://scout.docker.com/v/CVE-2025-22228?s=github&n=spring-security-crypto&ns=org.springframework.security&t=maven&vr=%3E%3D6.2.0%2C%3C%3D6.2.9"><img alt="high 7.4: CVE--2025--22228" src="https://img.shields.io/badge/CVE--2025--22228-lightgrey?label=high%207.4&labelColor=e25d68"/></a> <i>Improper Authentication</i>

<table>
<tr><td>Affected range</td><td><code>>=6.2.0<br/><=6.2.9</code></td></tr>
<tr><td>Fixed version</td><td><code>6.2.10</code></td></tr>
<tr><td>CVSS Score</td><td><code>7.4</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:3.1/AV:N/AC:H/PR:N/UI:N/S:U/C:H/I:H/A:N</code></td></tr>
<tr><td>EPSS Score</td><td><code>0.065%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>20th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

BCryptPasswordEncoder.matches(CharSequence,String) will incorrectly return true for passwords larger than 72 characters as long as the first 72 characters are the same.

</blockquote>
</details>
</details></td></tr>

<tr><td valign="top">
<details><summary><img alt="critical: 0" src="https://img.shields.io/badge/C-0-lightgrey"/> <img alt="high: 1" src="https://img.shields.io/badge/H-1-e25d68"/> <img alt="medium: 0" src="https://img.shields.io/badge/M-0-lightgrey"/> <img alt="low: 0" src="https://img.shields.io/badge/L-0-lightgrey"/> <!-- unspecified: 0 --><strong>com.mysql/mysql-connector-j</strong> <code>8.0.33</code> (maven)</summary>

<small><code>pkg:maven/com.mysql/mysql-connector-j@8.0.33</code></small><br/>
<a href="https://scout.docker.com/v/CVE-2023-22102?s=github&n=mysql-connector-j&ns=com.mysql&t=maven&vr=%3C8.2.0"><img alt="high 8.9: CVE--2023--22102" src="https://img.shields.io/badge/CVE--2023--22102-lightgrey?label=high%208.9&labelColor=e25d68"/></a> 

<table>
<tr><td>Affected range</td><td><code><8.2.0</code></td></tr>
<tr><td>Fixed version</td><td><code>8.2.0</code></td></tr>
<tr><td>CVSS Score</td><td><code>8.9</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:4.0/AV:N/AC:H/AT:P/PR:N/UI:A/VC:H/VI:H/VA:H/SC:H/SI:H/SA:H</code></td></tr>
<tr><td>EPSS Score</td><td><code>3.493%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>88th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

Vulnerability in the MySQL Connectors product of Oracle MySQL (component: Connector/J). Supported versions that are affected are 8.1.0 and prior. Difficult to exploit vulnerability allows unauthenticated attacker with network access via multiple protocols to compromise MySQL Connectors. Successful attacks require human interaction from a person other than the attacker and while the vulnerability is in MySQL Connectors, attacks may significantly impact additional products (scope change). Successful attacks of this vulnerability can result in takeover of MySQL Connectors.

</blockquote>
</details>
</details></td></tr>

<tr><td valign="top">
<details><summary><img alt="critical: 0" src="https://img.shields.io/badge/C-0-lightgrey"/> <img alt="high: 0" src="https://img.shields.io/badge/H-0-lightgrey"/> <img alt="medium: 2" src="https://img.shields.io/badge/M-2-fbb552"/> <img alt="low: 2" src="https://img.shields.io/badge/L-2-fce1a9"/> <!-- unspecified: 0 --><strong>ch.qos.logback/logback-core</strong> <code>1.4.14</code> (maven)</summary>

<small><code>pkg:maven/ch.qos.logback/logback-core@1.4.14</code></small><br/>
<a href="https://scout.docker.com/v/CVE-2025-11226?s=github&n=logback-core&ns=ch.qos.logback&t=maven&vr=%3E%3D1.4.0%2C%3C1.5.19"><img alt="medium 5.9: CVE--2025--11226" src="https://img.shields.io/badge/CVE--2025--11226-lightgrey?label=medium%205.9&labelColor=fbb552"/></a> <i>Improper Input Validation</i>

<table>
<tr><td>Affected range</td><td><code>>=1.4.0<br/><1.5.19</code></td></tr>
<tr><td>Fixed version</td><td><code>1.5.19</code></td></tr>
<tr><td>CVSS Score</td><td><code>5.9</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:4.0/AV:L/AC:L/AT:P/PR:H/UI:P/VC:H/VI:L/VA:L/SC:H/SI:L/SA:L</code></td></tr>
<tr><td>EPSS Score</td><td><code>0.062%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>19th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

QOS.CH logback-core versions up to 1.5.18 contain an ACE vulnerability in conditional configuration file processing in Java applications. This vulnerability allows an attacker to execute arbitrary code by compromising an existing logback configuration file or by injecting a malicious environment variable before program execution.

A successful attack requires the Janino library and Spring Framework to be present on the user's class path. Additionally, the attacker must have write access to a configuration file. Alternatively, the attacker could inject a malicious environment variable pointing to a malicious configuration file. In both cases, the attack requires existing privileges.

</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2024-12798?s=github&n=logback-core&ns=ch.qos.logback&t=maven&vr=%3E%3D1.4.0%2C%3C1.5.13"><img alt="medium 5.9: CVE--2024--12798" src="https://img.shields.io/badge/CVE--2024--12798-lightgrey?label=medium%205.9&labelColor=fbb552"/></a> <i>Improper Neutralization of Special Elements used in an Expression Language Statement ('Expression Language Injection')</i>

<table>
<tr><td>Affected range</td><td><code>>=1.4.0<br/><1.5.13</code></td></tr>
<tr><td>Fixed version</td><td><code>1.5.13</code></td></tr>
<tr><td>CVSS Score</td><td><code>5.9</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:4.0/AV:L/AC:L/AT:P/PR:L/UI:P/VC:L/VI:H/VA:L/SC:L/SI:H/SA:L/RE:L/U:Clear</code></td></tr>
<tr><td>EPSS Score</td><td><code>0.121%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>31st percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

ACE vulnerability in JaninoEventEvaluator by QOS.CH logback-core up to and including version 1.5.12 in Java applications allows attackers to execute arbitrary code by compromising an existing logback configuration file or by injecting an environment variable before program execution.

Malicious logback configuration files can allow the attacker to execute arbitrary code using the JaninoEventEvaluator extension.

A successful attack requires the user to have write access to a configuration file. Alternatively, the attacker could inject a malicious environment variable pointing to a malicious configuration file. In both cases, the attack requires existing privilege.

</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2024-12801?s=github&n=logback-core&ns=ch.qos.logback&t=maven&vr=%3E%3D1.4.0%2C%3C1.5.13"><img alt="low 2.4: CVE--2024--12801" src="https://img.shields.io/badge/CVE--2024--12801-lightgrey?label=low%202.4&labelColor=fce1a9"/></a> <i>Server-Side Request Forgery (SSRF)</i>

<table>
<tr><td>Affected range</td><td><code>>=1.4.0<br/><1.5.13</code></td></tr>
<tr><td>Fixed version</td><td><code>1.5.13</code></td></tr>
<tr><td>CVSS Score</td><td><code>2.4</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:4.0/AV:L/AC:L/AT:P/PR:L/UI:P/VC:L/VI:N/VA:L/SC:H/SI:H/SA:H/V:D/U:Clear</code></td></tr>
<tr><td>EPSS Score</td><td><code>0.046%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>14th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

Server-Side Request Forgery (SSRF) in SaxEventRecorder by QOS.CH logback version 1.5.12 on the Java platform, allows an attacker to forge requests by compromising logback configuration files in XML.
 
The attacks involves the modification of DOCTYPE declaration in  XML configuration files.

</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2026-1225?s=github&n=logback-core&ns=ch.qos.logback&t=maven&vr=%3C1.5.25"><img alt="low 1.8: CVE--2026--1225" src="https://img.shields.io/badge/CVE--2026--1225-lightgrey?label=low%201.8&labelColor=fce1a9"/></a> <i>Improper Input Validation</i>

<table>
<tr><td>Affected range</td><td><code><1.5.25</code></td></tr>
<tr><td>Fixed version</td><td><code>1.5.25</code></td></tr>
<tr><td>CVSS Score</td><td><code>1.8</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:4.0/AV:L/AC:H/AT:P/PR:H/UI:N/VC:L/VI:L/VA:L/SC:L/SI:L/SA:L</code></td></tr>
<tr><td>EPSS Score</td><td><code>0.012%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>2nd percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

ACE vulnerability in configuration file processing  by QOS.CH logback-core up to and including version 1.5.24 in Java applications, allows an attacker to instantiate classes already present on the class path by compromising an existing logback configuration file.

The instantiation of a potentially malicious Java class requires that said class is present on the user's class-path. In addition, the attacker must  have write access to a configuration file. However, after successful instantiation, the instance is very likely to be discarded with no further ado.

</blockquote>
</details>
</details></td></tr>

<tr><td valign="top">
<details><summary><img alt="critical: 0" src="https://img.shields.io/badge/C-0-lightgrey"/> <img alt="high: 0" src="https://img.shields.io/badge/H-0-lightgrey"/> <img alt="medium: 2" src="https://img.shields.io/badge/M-2-fbb552"/> <img alt="low: 1" src="https://img.shields.io/badge/L-1-fce1a9"/> <!-- unspecified: 0 --><strong>org.springframework/spring-context</strong> <code>6.1.5</code> (maven)</summary>

<small><code>pkg:maven/org.springframework/spring-context@6.1.5</code></small><br/>
<a href="https://scout.docker.com/v/CVE-2024-38820?s=github&n=spring-context&ns=org.springframework&t=maven&vr=%3E%3D6.1.0%2C%3C6.1.14"><img alt="medium 5.3: CVE--2024--38820" src="https://img.shields.io/badge/CVE--2024--38820-lightgrey?label=medium%205.3&labelColor=fbb552"/></a> <i>Improper Handling of Case Sensitivity</i>

<table>
<tr><td>Affected range</td><td><code>>=6.1.0<br/><6.1.14</code></td></tr>
<tr><td>Fixed version</td><td><code>6.1.14</code></td></tr>
<tr><td>CVSS Score</td><td><code>5.3</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:N/I:L/A:N</code></td></tr>
<tr><td>EPSS Score</td><td><code>1.514%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>81st percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

The fix for CVE-2022-22968 made disallowedFields patterns in DataBinder case insensitive. However, String.toLowerCase() has some Locale dependent exceptions that could potentially result in fields not protected as expected.

</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2024-38827?s=gitlab&n=spring-context&ns=org.springframework&t=maven&vr=%3C6.1.14"><img alt="medium 4.8: CVE--2024--38827" src="https://img.shields.io/badge/CVE--2024--38827-lightgrey?label=medium%204.8&labelColor=fbb552"/></a> <i>OWASP Top Ten 2017 Category A9 - Using Components with Known Vulnerabilities</i>

<table>
<tr><td>Affected range</td><td><code><6.1.14</code></td></tr>
<tr><td>Fixed version</td><td><code>6.1.14</code></td></tr>
<tr><td>CVSS Score</td><td><code>4.8</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:3.1/AV:N/AC:H/PR:N/UI:N/S:U/C:L/I:L/A:N</code></td></tr>
<tr><td>EPSS Score</td><td><code>0.410%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>61st percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

The usage of String.toLowerCase() and String.toUpperCase() has some Locale dependent exceptions that could potentially result in authorization rules not working properly.

</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2025-22233?s=github&n=spring-context&ns=org.springframework&t=maven&vr=%3E%3D6.1.0%2C%3C%3D6.1.19"><img alt="low 3.1: CVE--2025--22233" src="https://img.shields.io/badge/CVE--2025--22233-lightgrey?label=low%203.1&labelColor=fce1a9"/></a> <i>Improper Input Validation</i>

<table>
<tr><td>Affected range</td><td><code>>=6.1.0<br/><=6.1.19</code></td></tr>
<tr><td>Fixed version</td><td><code>6.1.20</code></td></tr>
<tr><td>CVSS Score</td><td><code>3.1</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:3.1/AV:N/AC:H/PR:L/UI:N/S:U/C:N/I:L/A:N</code></td></tr>
<tr><td>EPSS Score</td><td><code>0.083%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>24th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

CVE-2024-38820 ensured Locale-independent, lowercase conversion for both the configured disallowedFields patterns and for request parameter names. However, there are still cases where it is possible to bypass the disallowedFields checks.

Affected Spring Products and Versions

Spring Framework:
  *  6.2.0 - 6.2.6

  *  6.1.0 - 6.1.19

  *  6.0.0 - 6.0.27

  *  5.3.0 - 5.3.42
  *  Older, unsupported versions are also affected



Mitigation

Users of affected versions should upgrade to the corresponding fixed version.

| Affected version(s) | Fix Version | Availability |
| - | - | - |
| 6.2.x |  6.2.7 | OSS |
| 6.1.x |  6.1.20 | OSS |
| 6.0.x |  6.0.28 |  Commercial https://enterprise.spring.io/ |
| 5.3.x |  5.3.43 | Commercial https://enterprise.spring.io/  |

No further mitigation steps are necessary.


Generally, we recommend using a dedicated model object with properties only for data binding, or using constructor binding since constructor arguments explicitly declare what to bind together with turning off setter binding through the declarativeBinding flag. See the Model Design section in the reference documentation.

For setting binding, prefer the use of allowedFields (an explicit list) over disallowedFields.

Credit

This issue was responsibly reported by the TERASOLUNA Framework Development Team from NTT DATA Group Corporation.

</blockquote>
</details>
</details></td></tr>

<tr><td valign="top">
<details><summary><img alt="critical: 0" src="https://img.shields.io/badge/C-0-lightgrey"/> <img alt="high: 0" src="https://img.shields.io/badge/H-0-lightgrey"/> <img alt="medium: 2" src="https://img.shields.io/badge/M-2-fbb552"/> <img alt="low: 0" src="https://img.shields.io/badge/L-0-lightgrey"/> <!-- unspecified: 0 --><strong>io.netty/netty-common</strong> <code>4.1.107.Final</code> (maven)</summary>

<small><code>pkg:maven/io.netty/netty-common@4.1.107.Final</code></small><br/>
<a href="https://scout.docker.com/v/CVE-2025-25193?s=github&n=netty-common&ns=io.netty&t=maven&vr=%3C4.1.118.Final"><img alt="medium 5.5: CVE--2025--25193" src="https://img.shields.io/badge/CVE--2025--25193-lightgrey?label=medium%205.5&labelColor=fbb552"/></a> <i>Uncontrolled Resource Consumption</i>

<table>
<tr><td>Affected range</td><td><code><4.1.118.Final</code></td></tr>
<tr><td>Fixed version</td><td><code>4.1.118.Final</code></td></tr>
<tr><td>CVSS Score</td><td><code>5.5</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:3.1/AV:L/AC:L/PR:L/UI:N/S:U/C:N/I:N/A:H</code></td></tr>
<tr><td>EPSS Score</td><td><code>0.096%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>26th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

### Summary
An unsafe reading of environment file could potentially cause a denial of service in Netty.
When loaded on an Windows application, Netty attemps to load a file that does not exist. If an attacker creates such a large file, the Netty application crash.

### Details
A similar issue was previously reported in https://github.com/netty/netty/security/advisories/GHSA-xq3w-v528-46rv
This issue was fixed, but the fix was incomplete in that null-bytes were not counted against the input limit.


### PoC
The PoC is the same as for https://github.com/netty/netty/security/advisories/GHSA-xq3w-v528-46rv with the detail that the file should only contain null-bytes; 0x00.
When the null-bytes are encountered by the `InputStreamReader`, it will issue replacement characters in its charset decoding, which will fill up the line-buffer in the `BufferedReader.readLine()`, because the replacement character is not a line-break character.

### Impact
Impact is the same as https://github.com/netty/netty/security/advisories/GHSA-xq3w-v528-46rv

</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2024-47535?s=github&n=netty-common&ns=io.netty&t=maven&vr=%3C%3D4.1.114.Final"><img alt="medium 5.4: CVE--2024--47535" src="https://img.shields.io/badge/CVE--2024--47535-lightgrey?label=medium%205.4&labelColor=fbb552"/></a> <i>Uncontrolled Resource Consumption</i>

<table>
<tr><td>Affected range</td><td><code><=4.1.114.Final</code></td></tr>
<tr><td>Fixed version</td><td><code>4.1.115.Final</code></td></tr>
<tr><td>CVSS Score</td><td><code>5.4</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:4.0/AV:L/AC:L/AT:N/PR:L/UI:N/VC:N/VI:N/VA:H/SC:N/SI:N/SA:N/E:P</code></td></tr>
<tr><td>EPSS Score</td><td><code>0.467%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>65th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

### Summary

An unsafe reading of environment file could potentially cause a denial of service in Netty.
When loaded on an Windows application, Netty attemps to load a file that does not exist. If an attacker creates such a large file, the Netty application crash.


### Details

When the library netty is loaded in a java windows application, the library tries to identify the system environnement in which it is executed.

At this stage, Netty tries to load both `/etc/os-release` and `/usr/lib/os-release` even though it is in a Windows environment. 

<img width="364" alt="1" src="https://github.com/user-attachments/assets/9466b181-9394-45a3-b0e3-1dcf105def59">

If netty finds this files, it reads them and loads them into memory.

By default :

- The JVM maximum memory size is set to 1 GB,
- A non-privileged user can create a directory at `C:\` and create files within it.

<img width="340" alt="2" src="https://github.com/user-attachments/assets/43b359a2-5871-4592-ae2b-ffc40ac76831">

<img width="523" alt="3" src="https://github.com/user-attachments/assets/ad5c6eed-451c-4513-92d5-ba0eee7715c1">

the source code identified :
https://github.com/netty/netty/blob/4.1/common/src/main/java/io/netty/util/internal/PlatformDependent.java

Despite the implementation of the function `normalizeOs()` the source code not verify the OS before reading `C:\etc\os-release` and `C:\usr\lib\os-release`.

### PoC

Create a file larger than 1 GB of data in `C:\etc\os-release` or `C:\usr\lib\os-release` on a Windows environnement and start your Netty application.

To observe what the application does with the file, the security analyst used "Process Monitor" from the "Windows SysInternals" suite. (https://learn.microsoft.com/en-us/sysinternals/)

```
cd C:\etc
fsutil file createnew os-release 3000000000
```

<img width="519" alt="4" src="https://github.com/user-attachments/assets/39df22a3-462b-4fd0-af9a-aa30077ec08f">

<img width="517" alt="5" src="https://github.com/user-attachments/assets/129dbd50-fc36-4da5-8eb1-582123fb528f">

The source code used is the Netty website code example : [Echo ‐ the very basic client and server](https://netty.io/4.1/xref/io/netty/example/echo/package-summary.html).

The vulnerability was tested on the 4.1.112.Final version.

The security analyst tried the same technique for `C:\proc\sys\net\core\somaxconn` with a lot of values to impact Netty but the only things that works is the "larger than 1 GB file" technique. https://github.com/netty/netty/blob/c0fdb8e9f8f256990e902fcfffbbe10754d0f3dd/common/src/main/java/io/netty/util/NetUtil.java#L186

### Impact

By loading the "file larger than 1 GB" into the memory, the Netty library exceeds the JVM memory limit and causes a crash in the java Windows application.

This behaviour occurs 100% of the time in both Server mode and Client mode if the large file exists.

Client mode :

<img width="449" alt="6" src="https://github.com/user-attachments/assets/f8fe1ed0-1a42-4490-b9ed-dbc9af7804be">

Server mode :

<img width="464" alt="7" src="https://github.com/user-attachments/assets/b34b42bd-4fbd-4170-b93a-d29ba87b88eb">

somaxconn :

<img width="532" alt="8" src="https://github.com/user-attachments/assets/0656b3bb-32c6-4ae2-bff7-d93babba08a3">

### Severity

- Attack vector : "Local" because the attacker needs to be on the system where the Netty application is running.
- Attack complexity : "Low" because the attacker only need to create a massive file (regardless of its contents).
- Privileges required : "Low" because the attacker requires a user account to exploit the vulnerability.
- User intercation : "None" because the administrator don't need to accidentally click anywhere to trigger the vulnerability. Furthermore, the exploitation works with defaults windows/AD settings.
- Scope : "Unchanged" because only Netty is affected by the vulnerability.
- Confidentiality : "None" because no data is exposed through exploiting the vulnerability.
- Integrity : "None" because the explotation of the vulnerability does not allow editing, deleting or adding data elsewhere.
- Availability : "High" because the exploitation of this vulnerability crashes the entire java application.

</blockquote>
</details>
</details></td></tr>

<tr><td valign="top">
<details><summary><img alt="critical: 0" src="https://img.shields.io/badge/C-0-lightgrey"/> <img alt="high: 0" src="https://img.shields.io/badge/H-0-lightgrey"/> <img alt="medium: 1" src="https://img.shields.io/badge/M-1-fbb552"/> <img alt="low: 2" src="https://img.shields.io/badge/L-2-fce1a9"/> <!-- unspecified: 0 --><strong>busybox</strong> <code>1.36.1-r20</code> (apk)</summary>

<small><code>pkg:apk/alpine/busybox@1.36.1-r20?os_name=alpine&os_version=3.19</code></small><br/>
<a href="https://scout.docker.com/v/CVE-2025-60876?s=alpine&n=busybox&ns=alpine&t=apk&osn=alpine&osv=3.19&vr=%3C%3D1.36.1-r20"><img alt="medium : CVE--2025--60876" src="https://img.shields.io/badge/CVE--2025--60876-lightgrey?label=medium%20&labelColor=fbb552"/></a> 

<table>
<tr><td>Affected range</td><td><code><=1.36.1-r20</code></td></tr>
<tr><td>Fixed version</td><td><strong>Not Fixed</strong></td></tr>
<tr><td>EPSS Score</td><td><code>0.051%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>16th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>



</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2025-46394?s=alpine&n=busybox&ns=alpine&t=apk&osn=alpine&osv=3.19&vr=%3C1.36.1-r21"><img alt="low : CVE--2025--46394" src="https://img.shields.io/badge/CVE--2025--46394-lightgrey?label=low%20&labelColor=fce1a9"/></a> 

<table>
<tr><td>Affected range</td><td><code><1.36.1-r21</code></td></tr>
<tr><td>Fixed version</td><td><code>1.36.1-r21</code></td></tr>
<tr><td>EPSS Score</td><td><code>0.083%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>24th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>



</blockquote>
</details>

<a href="https://scout.docker.com/v/CVE-2024-58251?s=alpine&n=busybox&ns=alpine&t=apk&osn=alpine&osv=3.19&vr=%3C1.36.1-r21"><img alt="low : CVE--2024--58251" src="https://img.shields.io/badge/CVE--2024--58251-lightgrey?label=low%20&labelColor=fce1a9"/></a> 

<table>
<tr><td>Affected range</td><td><code><1.36.1-r21</code></td></tr>
<tr><td>Fixed version</td><td><code>1.36.1-r21</code></td></tr>
<tr><td>EPSS Score</td><td><code>0.077%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>23rd percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>



</blockquote>
</details>
</details></td></tr>

<tr><td valign="top">
<details><summary><img alt="critical: 0" src="https://img.shields.io/badge/C-0-lightgrey"/> <img alt="high: 0" src="https://img.shields.io/badge/H-0-lightgrey"/> <img alt="medium: 1" src="https://img.shields.io/badge/M-1-fbb552"/> <img alt="low: 0" src="https://img.shields.io/badge/L-0-lightgrey"/> <!-- unspecified: 0 --><strong>org.springframework/spring-jdbc</strong> <code>6.1.5</code> (maven)</summary>

<small><code>pkg:maven/org.springframework/spring-jdbc@6.1.5</code></small><br/>
<a href="https://scout.docker.com/v/CVE-2024-38827?s=gitlab&n=spring-jdbc&ns=org.springframework&t=maven&vr=%3C6.1.14"><img alt="medium 4.8: CVE--2024--38827" src="https://img.shields.io/badge/CVE--2024--38827-lightgrey?label=medium%204.8&labelColor=fbb552"/></a> <i>OWASP Top Ten 2017 Category A9 - Using Components with Known Vulnerabilities</i>

<table>
<tr><td>Affected range</td><td><code><6.1.14</code></td></tr>
<tr><td>Fixed version</td><td><code>6.1.14</code></td></tr>
<tr><td>CVSS Score</td><td><code>4.8</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:3.1/AV:N/AC:H/PR:N/UI:N/S:U/C:L/I:L/A:N</code></td></tr>
<tr><td>EPSS Score</td><td><code>0.410%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>61st percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

The usage of String.toLowerCase() and String.toUpperCase() has some Locale dependent exceptions that could potentially result in authorization rules not working properly.

</blockquote>
</details>
</details></td></tr>

<tr><td valign="top">
<details><summary><img alt="critical: 0" src="https://img.shields.io/badge/C-0-lightgrey"/> <img alt="high: 0" src="https://img.shields.io/badge/H-0-lightgrey"/> <img alt="medium: 1" src="https://img.shields.io/badge/M-1-fbb552"/> <img alt="low: 0" src="https://img.shields.io/badge/L-0-lightgrey"/> <!-- unspecified: 0 --><strong>com.fasterxml.jackson.core/jackson-core</strong> <code>2.15.4</code> (maven)</summary>

<small><code>pkg:maven/com.fasterxml.jackson.core/jackson-core@2.15.4</code></small><br/>
<a href="https://scout.docker.com/v/GHSA-72hv-8253-57qq?s=github&n=jackson-core&ns=com.fasterxml.jackson.core&t=maven&vr=%3E%3D2.0.0%2C%3C%3D2.18.5"><img alt="medium 6.9: GHSA--72hv--8253--57qq" src="https://img.shields.io/badge/GHSA--72hv--8253--57qq-lightgrey?label=medium%206.9&labelColor=fbb552"/></a> <i>Allocation of Resources Without Limits or Throttling</i>

<table>
<tr><td>Affected range</td><td><code>>=2.0.0<br/><=2.18.5</code></td></tr>
<tr><td>Fixed version</td><td><code>2.18.6</code></td></tr>
<tr><td>CVSS Score</td><td><code>6.9</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:4.0/AV:N/AC:L/AT:N/PR:N/UI:N/VC:N/VI:N/VA:L/SC:N/SI:N/SA:N</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

### Summary
The non-blocking (async) JSON parser in `jackson-core` bypasses the `maxNumberLength` constraint (default: 1000 characters) defined in `StreamReadConstraints`. This allows an attacker to send JSON with arbitrarily long numbers through the async parser API, leading to excessive memory allocation and potential CPU exhaustion, resulting in a Denial of Service (DoS).

The standard synchronous parser correctly enforces this limit, but the async parser fails to do so, creating an inconsistent enforcement policy.

### Details
The root cause is that the async parsing path in `NonBlockingUtf8JsonParserBase` (and related classes) does not call the methods responsible for number length validation.

- The number parsing methods (e.g., `_finishNumberIntegralPart`) accumulate digits into the `TextBuffer` without any length checks.
- After parsing, they call `_valueComplete()`, which finalizes the token but does **not** call `resetInt()` or `resetFloat()`.
- The `resetInt()`/`resetFloat()` methods in `ParserBase` are where the `validateIntegerLength()` and `validateFPLength()` checks are performed.
- Because this validation step is skipped, the `maxNumberLength` constraint is never enforced in the async code path.

### PoC
The following JUnit 5 test demonstrates the vulnerability. It shows that the async parser accepts a 5,000-digit number, whereas the limit should be 1,000.

```java
package tools.jackson.core.unittest.dos;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import tools.jackson.core.*;
import tools.jackson.core.exc.StreamConstraintsException;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.core.json.async.NonBlockingByteArrayJsonParser;

import static org.junit.jupiter.api.Assertions.*;

/**
 * POC: Number Length Constraint Bypass in Non-Blocking (Async) JSON Parsers
 *
 * Authors: sprabhav7, rohan-repos
 * 
 * maxNumberLength default = 1000 characters (digits).
 * A number with more than 1000 digits should be rejected by any parser.
 *
 * BUG: The async parser never calls resetInt()/resetFloat() which is where
 * validateIntegerLength()/validateFPLength() lives. Instead it calls
 * _valueComplete() which skips all number length validation.
 *
 * CWE-770: Allocation of Resources Without Limits or Throttling
 */
class AsyncParserNumberLengthBypassTest {

    private static final int MAX_NUMBER_LENGTH = 1000;
    private static final int TEST_NUMBER_LENGTH = 5000;

    private final JsonFactory factory = new JsonFactory();

    // CONTROL: Sync parser correctly rejects a number exceeding maxNumberLength
    @<!-- -->Test
    void syncParserRejectsLongNumber() throws Exception {
        byte[] payload = buildPayloadWithLongInteger(TEST_NUMBER_LENGTH);
		
		// Output to console
        System.out.println("[SYNC] Parsing " + TEST_NUMBER_LENGTH + "-digit number (limit: " + MAX_NUMBER_LENGTH + ")");
        try {
            try (JsonParser p = factory.createParser(ObjectReadContext.empty(), payload)) {
                while (p.nextToken() != null) {
                    if (p.currentToken() == JsonToken.VALUE_NUMBER_INT) {
                        System.out.println("[SYNC] Accepted number with " + p.getText().length() + " digits — UNEXPECTED");
                    }
                }
            }
            fail("Sync parser must reject a " + TEST_NUMBER_LENGTH + "-digit number");
        } catch (StreamConstraintsException e) {
            System.out.println("[SYNC] Rejected with StreamConstraintsException: " + e.getMessage());
        }
    }

    // VULNERABILITY: Async parser accepts the SAME number that sync rejects
    @<!-- -->Test
    void asyncParserAcceptsLongNumber() throws Exception {
        byte[] payload = buildPayloadWithLongInteger(TEST_NUMBER_LENGTH);

        NonBlockingByteArrayJsonParser p =
            (NonBlockingByteArrayJsonParser) factory.createNonBlockingByteArrayParser(ObjectReadContext.empty());
        p.feedInput(payload, 0, payload.length);
        p.endOfInput();

        boolean foundNumber = false;
        try {
            while (p.nextToken() != null) {
                if (p.currentToken() == JsonToken.VALUE_NUMBER_INT) {
                    foundNumber = true;
                    String numberText = p.getText();
                    assertEquals(TEST_NUMBER_LENGTH, numberText.length(),
                        "Async parser silently accepted all " + TEST_NUMBER_LENGTH + " digits");
                }
            }
            // Output to console
            System.out.println("[ASYNC INT] Accepted number with " + TEST_NUMBER_LENGTH + " digits — BUG CONFIRMED");
            assertTrue(foundNumber, "Parser should have produced a VALUE_NUMBER_INT token");
        } catch (StreamConstraintsException e) {
            fail("Bug is fixed — async parser now correctly rejects long numbers: " + e.getMessage());
        }
        p.close();
    }

    private byte[] buildPayloadWithLongInteger(int numDigits) {
        StringBuilder sb = new StringBuilder(numDigits + 10);
        sb.append("{\"v\":");
        for (int i = 0; i < numDigits; i++) {
            sb.append((char) ('1' + (i % 9)));
        }
        sb.append('}');
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }
}

```


### Impact
A malicious actor can send a JSON document with an arbitrarily long number to an application using the async parser (e.g., in a Spring WebFlux or other reactive application). This can cause:
1.  **Memory Exhaustion:** Unbounded allocation of memory in the `TextBuffer` to store the number's digits, leading to an `OutOfMemoryError`.
2.  **CPU Exhaustion:** If the application subsequently calls `getBigIntegerValue()` or `getDecimalValue()`, the JVM can be tied up in O(n^2) `BigInteger` parsing operations, leading to a CPU-based DoS.

### Suggested Remediation

The async parsing path should be updated to respect the `maxNumberLength` constraint. The simplest fix appears to ensure that `_valueComplete()` or a similar method in the async path calls the appropriate validation methods (`resetInt()` or `resetFloat()`) already present in `ParserBase`, mirroring the behavior of the synchronous parsers.

**NOTE:** This research was performed in collaboration with [rohan-repos](https://github.com/rohan-repos)

</blockquote>
</details>
</details></td></tr>

<tr><td valign="top">
<details><summary><img alt="critical: 0" src="https://img.shields.io/badge/C-0-lightgrey"/> <img alt="high: 0" src="https://img.shields.io/badge/H-0-lightgrey"/> <img alt="medium: 1" src="https://img.shields.io/badge/M-1-fbb552"/> <img alt="low: 0" src="https://img.shields.io/badge/L-0-lightgrey"/> <!-- unspecified: 0 --><strong>org.springframework/spring-websocket</strong> <code>6.1.5</code> (maven)</summary>

<small><code>pkg:maven/org.springframework/spring-websocket@6.1.5</code></small><br/>
<a href="https://scout.docker.com/v/CVE-2025-41254?s=github&n=spring-websocket&ns=org.springframework&t=maven&vr=%3E%3D6.1.0%2C%3C%3D6.1.21"><img alt="medium 4.3: CVE--2025--41254" src="https://img.shields.io/badge/CVE--2025--41254-lightgrey?label=medium%204.3&labelColor=fbb552"/></a> <i>Cross-Site Request Forgery (CSRF)</i>

<table>
<tr><td>Affected range</td><td><code>>=6.1.0<br/><=6.1.21</code></td></tr>
<tr><td>Fixed version</td><td><strong>Not Fixed</strong></td></tr>
<tr><td>CVSS Score</td><td><code>4.3</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:3.1/AV:N/AC:L/PR:N/UI:R/S:U/C:N/I:L/A:N</code></td></tr>
<tr><td>EPSS Score</td><td><code>0.060%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>19th percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

STOMP over WebSocket applications may be vulnerable to a security bypass that allows an attacker to send unauthorized messages.

### Affected Spring Products and Versions
Spring Framework:

  *  6.2.0 - 6.2.11
  *  6.1.0 - 6.1.23
  *  6.0.x - 6.0.29
  *  5.3.0 - 5.3.45
  *  Older, unsupported versions are also affected.


### Mitigation
Users of affected versions should upgrade to the corresponding fixed version.

### Affected version(s)
Fix version | Availability
-|-
6.2.x | 6.2.12 OSS
6.1.x | 6.1.24 Commercial https://enterprise.spring.io/
6.0.x | N/A Out of support https://spring.io/projects/spring-framework#support
5.3.x | 5.3.46 Commercial https://enterprise.spring.io/

No further mitigation steps are necessary.

CreditThis vulnerability was discovered and responsibly reported by Jannis Kaiser.

</blockquote>
</details>
</details></td></tr>

<tr><td valign="top">
<details><summary><img alt="critical: 0" src="https://img.shields.io/badge/C-0-lightgrey"/> <img alt="high: 0" src="https://img.shields.io/badge/H-0-lightgrey"/> <img alt="medium: 1" src="https://img.shields.io/badge/M-1-fbb552"/> <img alt="low: 0" src="https://img.shields.io/badge/L-0-lightgrey"/> <!-- unspecified: 0 --><strong>org.springframework/spring-expression</strong> <code>6.1.5</code> (maven)</summary>

<small><code>pkg:maven/org.springframework/spring-expression@6.1.5</code></small><br/>
<a href="https://scout.docker.com/v/CVE-2024-38827?s=gitlab&n=spring-expression&ns=org.springframework&t=maven&vr=%3C6.1.14"><img alt="medium 4.8: CVE--2024--38827" src="https://img.shields.io/badge/CVE--2024--38827-lightgrey?label=medium%204.8&labelColor=fbb552"/></a> <i>OWASP Top Ten 2017 Category A9 - Using Components with Known Vulnerabilities</i>

<table>
<tr><td>Affected range</td><td><code><6.1.14</code></td></tr>
<tr><td>Fixed version</td><td><code>6.1.14</code></td></tr>
<tr><td>CVSS Score</td><td><code>4.8</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:3.1/AV:N/AC:H/PR:N/UI:N/S:U/C:L/I:L/A:N</code></td></tr>
<tr><td>EPSS Score</td><td><code>0.410%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>61st percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

The usage of String.toLowerCase() and String.toUpperCase() has some Locale dependent exceptions that could potentially result in authorization rules not working properly.

</blockquote>
</details>
</details></td></tr>

<tr><td valign="top">
<details><summary><img alt="critical: 0" src="https://img.shields.io/badge/C-0-lightgrey"/> <img alt="high: 0" src="https://img.shields.io/badge/H-0-lightgrey"/> <img alt="medium: 1" src="https://img.shields.io/badge/M-1-fbb552"/> <img alt="low: 0" src="https://img.shields.io/badge/L-0-lightgrey"/> <!-- unspecified: 0 --><strong>org.springframework/spring-beans</strong> <code>6.1.5</code> (maven)</summary>

<small><code>pkg:maven/org.springframework/spring-beans@6.1.5</code></small><br/>
<a href="https://scout.docker.com/v/CVE-2024-38827?s=gitlab&n=spring-beans&ns=org.springframework&t=maven&vr=%3C6.1.14"><img alt="medium 4.8: CVE--2024--38827" src="https://img.shields.io/badge/CVE--2024--38827-lightgrey?label=medium%204.8&labelColor=fbb552"/></a> <i>OWASP Top Ten 2017 Category A9 - Using Components with Known Vulnerabilities</i>

<table>
<tr><td>Affected range</td><td><code><6.1.14</code></td></tr>
<tr><td>Fixed version</td><td><code>6.1.14</code></td></tr>
<tr><td>CVSS Score</td><td><code>4.8</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:3.1/AV:N/AC:H/PR:N/UI:N/S:U/C:L/I:L/A:N</code></td></tr>
<tr><td>EPSS Score</td><td><code>0.410%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>61st percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

The usage of String.toLowerCase() and String.toUpperCase() has some Locale dependent exceptions that could potentially result in authorization rules not working properly.

</blockquote>
</details>
</details></td></tr>

<tr><td valign="top">
<details><summary><img alt="critical: 0" src="https://img.shields.io/badge/C-0-lightgrey"/> <img alt="high: 0" src="https://img.shields.io/badge/H-0-lightgrey"/> <img alt="medium: 1" src="https://img.shields.io/badge/M-1-fbb552"/> <img alt="low: 0" src="https://img.shields.io/badge/L-0-lightgrey"/> <!-- unspecified: 0 --><strong>org.springframework.security/spring-security-core</strong> <code>6.2.3</code> (maven)</summary>

<small><code>pkg:maven/org.springframework.security/spring-security-core@6.2.3</code></small><br/>
<a href="https://scout.docker.com/v/CVE-2024-38827?s=github&n=spring-security-core&ns=org.springframework.security&t=maven&vr=%3E%3D6.2.0%2C%3C6.2.8"><img alt="medium 6.3: CVE--2024--38827" src="https://img.shields.io/badge/CVE--2024--38827-lightgrey?label=medium%206.3&labelColor=fbb552"/></a> <i>Authorization Bypass Through User-Controlled Key</i>

<table>
<tr><td>Affected range</td><td><code>>=6.2.0<br/><6.2.8</code></td></tr>
<tr><td>Fixed version</td><td><code>6.2.8</code></td></tr>
<tr><td>CVSS Score</td><td><code>6.3</code></td></tr>
<tr><td>CVSS Vector</td><td><code>CVSS:4.0/AV:N/AC:L/AT:P/PR:N/UI:N/VC:L/VI:L/VA:N/SC:N/SI:N/SA:N</code></td></tr>
<tr><td>EPSS Score</td><td><code>0.410%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>61st percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>

The usage of String.toLowerCase() and String.toUpperCase() has some Locale dependent exceptions that could potentially result in authorization rules not working properly.

</blockquote>
</details>
</details></td></tr>

<tr><td valign="top">
<details><summary><img alt="critical: 0" src="https://img.shields.io/badge/C-0-lightgrey"/> <img alt="high: 0" src="https://img.shields.io/badge/H-0-lightgrey"/> <img alt="medium: 1" src="https://img.shields.io/badge/M-1-fbb552"/> <img alt="low: 0" src="https://img.shields.io/badge/L-0-lightgrey"/> <!-- unspecified: 0 --><strong>fontconfig</strong> <code>2.14.2-r4</code> (apk)</summary>

<small><code>pkg:apk/alpine/fontconfig@2.14.2-r4?os_name=alpine&os_version=3.19</code></small><br/>
<a href="https://scout.docker.com/v/CVE-2026-34085?s=alpine&n=fontconfig&ns=alpine&t=apk&osn=alpine&osv=3.19&vr=%3C%3D2.14.2-r4"><img alt="medium : CVE--2026--34085" src="https://img.shields.io/badge/CVE--2026--34085-lightgrey?label=medium%20&labelColor=fbb552"/></a> 

<table>
<tr><td>Affected range</td><td><code><=2.14.2-r4</code></td></tr>
<tr><td>Fixed version</td><td><strong>Not Fixed</strong></td></tr>
<tr><td>EPSS Score</td><td><code>0.014%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>3rd percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>



</blockquote>
</details>
</details></td></tr>

<tr><td valign="top">
<details><summary><img alt="critical: 0" src="https://img.shields.io/badge/C-0-lightgrey"/> <img alt="high: 0" src="https://img.shields.io/badge/H-0-lightgrey"/> <img alt="medium: 1" src="https://img.shields.io/badge/M-1-fbb552"/> <img alt="low: 0" src="https://img.shields.io/badge/L-0-lightgrey"/> <!-- unspecified: 0 --><strong>freetype</strong> <code>2.13.2-r0</code> (apk)</summary>

<small><code>pkg:apk/alpine/freetype@2.13.2-r0?os_name=alpine&os_version=3.19</code></small><br/>
<a href="https://scout.docker.com/v/CVE-2026-23865?s=alpine&n=freetype&ns=alpine&t=apk&osn=alpine&osv=3.19&vr=%3C%3D2.13.2-r0"><img alt="medium : CVE--2026--23865" src="https://img.shields.io/badge/CVE--2026--23865-lightgrey?label=medium%20&labelColor=fbb552"/></a> 

<table>
<tr><td>Affected range</td><td><code><=2.13.2-r0</code></td></tr>
<tr><td>Fixed version</td><td><strong>Not Fixed</strong></td></tr>
<tr><td>EPSS Score</td><td><code>0.015%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>3rd percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>



</blockquote>
</details>
</details></td></tr>

<tr><td valign="top">
<details><summary><img alt="critical: 0" src="https://img.shields.io/badge/C-0-lightgrey"/> <img alt="high: 0" src="https://img.shields.io/badge/H-0-lightgrey"/> <img alt="medium: 0" src="https://img.shields.io/badge/M-0-lightgrey"/> <img alt="low: 1" src="https://img.shields.io/badge/L-1-fce1a9"/> <!-- unspecified: 0 --><strong>zlib</strong> <code>1.3.1-r0</code> (apk)</summary>

<small><code>pkg:apk/alpine/zlib@1.3.1-r0?os_name=alpine&os_version=3.19</code></small><br/>
<a href="https://scout.docker.com/v/CVE-2026-27171?s=alpine&n=zlib&ns=alpine&t=apk&osn=alpine&osv=3.19&vr=%3C%3D1.3.1-r0"><img alt="low : CVE--2026--27171" src="https://img.shields.io/badge/CVE--2026--27171-lightgrey?label=low%20&labelColor=fce1a9"/></a> 

<table>
<tr><td>Affected range</td><td><code><=1.3.1-r0</code></td></tr>
<tr><td>Fixed version</td><td><strong>Not Fixed</strong></td></tr>
<tr><td>EPSS Score</td><td><code>0.009%</code></td></tr>
<tr><td>EPSS Percentile</td><td><code>1st percentile</code></td></tr>
</table>

<details><summary>Description</summary>
<blockquote>



</blockquote>
</details>
</details></td></tr>
</table>

