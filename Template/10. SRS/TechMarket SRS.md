Mr. Ngo - <u>uit@gm.uit.edu.vn</u>

Mr. Dinh - , uit@gm.uit.edu.vn

*Prepared for*

TechMarket Project

**Version 1.0**

**<span class="smallcaps">SOFTWARE REQUIREMENTS SPECIFICATION</span>**

TechMarket

**Revision and Sign Off Sheet**

**Change Record**

|  |  |  |  |
|:--:|:--:|:--:|:--:|
| **Author** | **Version** | **Change reference** | **Date** |
| Khai Ngo Quang | 0.1 | Initialize | 5/12/2023 |
| Khai Ngo Quang | 0.2 | Add some use case description | 10/12/2023 |
| Duong Dinh Quang | 0.3 | Update use case diagram, introduction and use case description | 20/12/2023 |
| Khai Ngo Quang | 0.4 | Update use case diagram, business rules, message list | 08/1/2023 |
| Duong Dinh Quang | 1.0 | Upload list and view description | 08/1/2023 |

**Reviewers**

|                |             |                   |            |
|:--------------:|:-----------:|:-----------------:|:----------:|
|    **Name**    | **Version** |   **Position**    |  **Date**  |
| Khai Ngo Quang |     0.1     | Application Owner | 6/12/2023  |
| Khai Ngo Quang |     0.2     | Application Owner | 11/12/2023 |
| Khai Ngo Quang |     0.3     | Application Owner | 21/12/2023 |
| Khai Ngo Quang |     0.4     | Application Owner | 08/1/2023  |
| Khai Ngo Quang |     1.0     | Application Owner | 08/1/2023  |

**Table of Contents**

[**1. Introduction 5**](#introduction)

> [1.1. Purpose 5](#purpose)
>
> [1.2. Scope 5](#scope)
>
> [1.3. Intended Audiences and Document Organization
> 5](#intended-audiences-and-document-organization)

[2. Functional Requirements 7](#_heading=)

[**2.1. Use Case Description 7**](#use-case-description)

> [UC1: Sign In 7](#uc1-manage-roles-and-audit-logs)
>
> [Activities Flow 8](#_heading=)
>
> [Business Rules 8](#business-rules)
>
> [UC2: Sign Up 9](#uc2-sign-up)
>
> [Activities Flow 10](#activities-flow-1)
>
> [Business Rules 10](#business-rules-1)
>
> [UC3: Forgot Password 13](#uc3-forgot-password)
>
> [Activities Flow 14](#activities-flow-2)
>
> [Business Rules 15](#business-rules-2)
>
> [UC4: Leave a feedback 18](#uc4-leave-a-feedback)
>
> [Activities Flow 19](#activities-flow-3)
>
> [Business Rules 19](#business-rules-3)
>
> [UC5: Create post 20](#uc5-create-post)
>
> [Activities Flow 20](#_heading=)
>
> [Business Rules 21](#_heading=)
>
> [UC6: Update post 22](#uc6-update-post)
>
> [Activities Flow 23](#_heading=)
>
> [Business Rules 23](#business-rules-5)
>
> [UC7: Delete post 25](#uc7-delete-post)
>
> [Activities Flow 25](#_heading=)
>
> [Business Rules 26](#business-rules-6)
>
> [UC8: Guarantee payment 26](#uc8-guarantee-payment)
>
> [Activities Flow 27](#activities-flow-7)
>
> [Business Rules 27](#business-rules-7)
>
> [UC9: Link sales wallet 28](#uc9-link-sales-wallet)
>
> [Activities Flow 29](#activities-flow-8)
>
> [Business Rules 29](#business-rules-8)
>
> [UC10: Use post push service 31](#uc10-use-post-push-service)
>
> [Activities Flow 32](#activities-flow-9)
>
> [Business Rules 33](#business-rules-9)
>
> [UC11: Confirm sales order 35](#uc11-confirm-sales-order)
>
> [Activities Flow 35](#activities-flow-10)
>
> [Business Rules 36](#business-rules-10)
>
> [UC12: Place order 37](#uc12-place-order)
>
> [Activities Flow 38](#activities-flow-11)
>
> [Business Rules 38](#business-rules-11)
>
> [UC13: Choose shipping address 41](#uc13-choose-shipping-address)
>
> [Activities Flow 42](#activities-flow-12)
>
> [Business Rules 42](#business-rules-12)
>
> [UC14: Pay 43](#uc14-pay)
>
> [Activities Flow 44](#_heading=)
>
> [Business Rules 44](#business-rules-13)
>
> [UC15: Cancel order 48](#uc15-cancel-order)
>
> [Activities Flow 49](#activities-flow-14)
>
> [Business Rules 49](#business-rules-14)
>
> [UC16: Confirm delivery of order 50](#uc16-confirm-delivery-of-order)
>
> [Activities Flow 50](#_heading=)
>
> [Business Rules 51](#business-rules-15)
>
> [UC17: Rate seller 51](#uc17-rate-seller)
>
> [Activities Flow 52](#activities-flow-16)
>
> [Business Rules 52](#business-rules-16)
>
> [UC18: Chat with seller 54](#uc18-chat-with-seller)
>
> [Activities Flow 54](#activities-flow-17)
>
> [Business Rules 55](#business-rules-17)
>
> [UC19: Report post 55](#uc19-report-post)
>
> [Activities Flow 56](#activities-flow-18)
>
> [Business Rules 56](#business-rules-18)
>
> [UC20: Create category 57](#uc20-create-category)
>
> [Activities Flow 58](#activities-flow-19)
>
> [Business Rules 58](#business-rules-19)
>
> [UC21: Update category 60](#uc21-update-category)
>
> [Activities Flow 60](#activities-flow-20)
>
> [Business Rules 61](#business-rules-20)
>
> [UC22: Delete category 62](#uc22-delete-category)
>
> [Activities Flow 62](#activities-flow-21)
>
> [Business Rules 62](#business-rules-21)
>
> [UC23: Approve post 63](#uc23-approve-post)
>
> [Activities Flow 64](#activities-flow-22)
>
> [Business Rules 64](#business-rules-22)
>
> [UC24: Reject post 65](#uc24-reject-post)
>
> [Activities Flow 66](#activities-flow-23)
>
> [Business Rules 66](#business-rules-23)
>
> [UC25: Ban account 67](#uc25-ban-account)
>
> [Activities Flow 67](#activities-flow-24)
>
> [Business Rules 68](#business-rules-24)
>
> [UC26: Manage TechMarket bank account
> 71](#uc26-manage-techmarket-bank-account)
>
> [Activities Flow 72](#activities-flow-25)
>
> [Business Rules 72](#business-rules-25)
>
> [UC27: Update legal documents 73](#uc27-update-legal-documents)
>
> [Activities Flow 74](#activities-flow-26)
>
> [Business Rules 74](#business-rules-26)
>
> [UC28: Update information for the help center
> 75](#uc28-update-information-for-the-help-center)
>
> [Activities Flow 76](#activities-flow-27)
>
> [Business Rules 76](#business-rules-27)
>
> [UC29: Handle feedback from user 77](#uc29-handle-feedback-from-user)
>
> [Activities Flow 77](#activities-flow-28)
>
> [Business Rules 78](#business-rules-28)

[**2.2. List Description 78**](#list-description)

[**2.3. View Description 78**](#view-description)

[**3. Non-functional Requirements 78**](#non-functional-requirements)

> [3.1. User Access and Security 78](#user-access-and-security)
>
> [3.2. Performance Requirements 79](#performance-requirements)
>
> [3.3. Implementation Requirements 80](#implementation-requirements)

[**4. Appendixes 80**](#appendixes)

> [Glossary 80](#glossary)
>
> [Messages 81](#messages)
>
> [Issues List 83](#issues-list)

# Introduction

## **Purpose**

This document serves as the comprehensive Software Requirements
Specification (SRS) and Design Document for the TechMarket project. It
encapsulates the detailed requirements and design considerations that
will guide the development process. This document is an essential
reference for developers, providing a roadmap for application
functionality, task assignment, and deployment strategies.

The primary purpose of this document is to outline the software
requirements for the TechMarket project and establish a clear design
framework. It acts as a foundational guide for developers, project
managers, and other stakeholders involved in the software development
lifecycle. By detailing the functionalities and design principles, this
document ensures a shared understanding of project goals and
expectations.

##  **Scope**

The scope of this document encompasses both the functional and
non-functional requirements of the TechMarket project. It defines how
the applications under development will operate, outlining features,
constraints, and interfaces. The scope extends to cover various aspects,
including user interactions, system performance, security, and
deployment considerations.

## **Intended Audiences and Document Organization**

This comprehensive document outlines the roles and responsibilities of
various teams involved in the TechMarket project. The project
encompasses the development, documentation, and user acceptance testing
(UAT) of the application. Each team plays a crucial role in ensuring the
success of the project, and this document aims to provide a detailed
overview of their responsibilities.

This document is intended for:

- Development team: The development team is at the forefront of
  transforming project requirements into functional, high-quality
  software. Their responsibilities extend from detailed design to
  implementation and testing at various levels.

- Documentation Team: The documentation team plays a critical role in
  creating user-friendly and informative documentation that accompanies
  the TechMarket application. Their work contributes to user
  understanding, efficient onboarding, and successful application usage.

- UAT team: The UAT team is responsible for validating the application's
  functionality and usability from an end-user perspective. Their role
  is crucial in ensuring that the application meets user expectations
  and requirements.

Below are main sections of the document:

- **1. Introduction**: This section describes the general introduction
  of this document.

- **2. Functional Requirements**: This section describes the functional
  requirements in detail.

- **3. Non-functional Requirements:** This section describes the
  non-functional requirements of this application such as user access
  and security, interfaces, screens and performance.

- **4. Other Requirements:** This section describes other requirements
  such as archive or security audit function.

- **5. Appendixes**: This section describes other requirements for this
  application and other supporting information for this document**.**

##  

# Functional Requirements

# Use Case Description

### UC1: Manage Roles and Audit Logs

|  |  |
|----|----|
| **Name** | Manage Role-Based Access Control (RBAC) |
| **Description** | This use case details how a Super Admin creates a single-function custom role, assigns it to a staff member, and handles role revocation to enforce strict access control. |
| **Actor** | Super Admin |
| **Trigger** | The Super Admin clicks on "Access Management" and selects "Create Role" or "Manage Staff". |
| **Pre-condition** | The Super Admin is logged in with highest-tier privileges. |
| **Post-condition** | The role is created/assigned, the staff's access token is updated, and the action is recorded in the immutable audit log. |

#### Activities Flow

<img src="media/image1.png" style="width:6.69272in;height:4.31944in" />

#### Business Rules

<table>
<colgroup>
<col style="width: 14%" />
<col style="width: 13%" />
<col style="width: 71%" />
</colgroup>
<tbody>
<tr>
<td style="text-align: center;"><strong>Activity</strong></td>
<td style="text-align: center;"><strong>BR Code</strong></td>
<td style="text-align: center;"><strong>Description</strong></td>
</tr>
<tr>
<td style="text-align: center;"><em>(3)</em></td>
<td style="text-align: center;"><em>BR1</em></td>
<td style="text-align: center;"><p><strong>Validate Rules:</strong></p>
<ul>
<li><p>The system checks the items [username], [password].</p></li>
<li><p>If any of them is null or blank the system will show an error
message MSG 2.</p></li>
<li><p>If [username] does not exist the system will show an error
message MSG 22 else [user] = User Repository find by [username] (call
findById() function)</p></li>
<li><p>If hash([password]) != user.password then the system will show an
error message MSG 22</p></li>
</ul>
<blockquote>
<p>else generate jwt from [user.id] and record this login session.</p>
</blockquote></td>
</tr>
<tr>
<td style="text-align: center;"><em>(4)</em></td>
<td style="text-align: center;"><em>BR2</em></td>
<td style="text-align: center;"><p><strong>Message Rules:</strong></p>
<ul>
<li><p>The system shows the error message MSG 22.</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(5)</em></td>
<td style="text-align: center;"><em>BR3</em></td>
<td style="text-align: center;"><p><strong>Message Rules:</strong></p>
<ul>
<li><p>The system shows the success message MSG 23.</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(6)</em></td>
<td style="text-align: center;"><em>BR4</em></td>
<td style="text-align: center;"><p><strong>Redirect Rules:</strong></p>
<ul>
<li><p>The system redirects to the home page.</p></li>
</ul></td>
</tr>
</tbody>
</table>

### UC2: Sign Up

<table>
<colgroup>
<col style="width: 21%" />
<col style="width: 78%" />
</colgroup>
<tbody>
<tr>
<td><strong>Name</strong></td>
<td><strong>Sign Up</strong></td>
</tr>
<tr>
<td><strong>Description</strong></td>
<td>This use case describes the process by which a user creates a new
account in the system</td>
</tr>
<tr>
<td><strong>Actor</strong></td>
<td>User</td>
</tr>
<tr>
<td><strong>Trigger</strong></td>
<td><ul>
<li><p>When the user clicks on the “Sign Up” button.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Pre-condition</strong></td>
<td><ul>
<li><p>The user is on the sign up page (refer to “Sign Up Form” in “List
description” file).</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Post-condition</strong></td>
<td><ul>
<li><p>New account has been created in the ‘INACTIVE’ state.</p></li>
<li><p>The user will be redirected to the home page.</p></li>
<li><p>The user will be asked to verify through email.</p></li>
</ul></td>
</tr>
</tbody>
</table>

#### Activities Flow

<img src="media/image2.png" style="width:6.69272in;height:3.875in" />

#### Business Rules

<table>
<colgroup>
<col style="width: 14%" />
<col style="width: 13%" />
<col style="width: 71%" />
</colgroup>
<tbody>
<tr>
<td style="text-align: center;"><strong>Activity</strong></td>
<td style="text-align: center;"><strong>BR Code</strong></td>
<td style="text-align: center;"><strong>Description</strong></td>
</tr>
<tr>
<td style="text-align: center;"><em>(3)</em></td>
<td style="text-align: center;"><em>BR5</em></td>
<td style="text-align: center;"><p><strong>Validate Rules:</strong></p>
<ul>
<li><p>The system checks the items [username], [password],
[phoneNumber], [email].</p></li>
<li><p>If any entries are empty, the system shows an error message MSG
2.</p></li>
<li><p>If [username.length] &lt; 8 then the system shows an error
message MSG 24.</p></li>
<li><p>If
pattern.compile(“"^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[@#$%^&amp;+=!])(?=\\S+$).{8,}$"”).notMatch([password])
then the system shows an error message MSG 25.</p></li>
<li><p>If
pattern.compile(‘^(84|0[3|5|7|8|9])+([0-9]{8})$’).notMatch([phoneNumber])
or then returns 400-BAD_REQUEST error with error message MSG 30</p></li>
<li><p>If
pattern.compile(‘^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$’).notMatch([email])
then returns 400-BAD_REQUEST error with error message MSG31.</p></li>
<li><p>If [phoneNumber] exists in the system then the system shows an
error message MSG 26.</p></li>
<li><p>If [email] exists in the system then the system shows an error
message MSG 27.</p></li>
<li><p>[user] = User Repository save new user with all data (call save()
function)</p></li>
<li><p>[user.status] = ‘INACTIVE’</p></li>
<li><p>The system will show a success message MSG 28.</p></li>
<li><p>Send verify email as <strong>Email Templates</strong>
below,</p></li>
<li><p><strong>Email Templates:</strong></p></li>
<li><p>Send mail to user register account as the template
below<strong>:</strong></p></li>
</ul>
<table>
<colgroup>
<col style="width: 25%" />
<col style="width: 74%" />
</colgroup>
<tbody>
<tr>
<td><blockquote>
<p>From</p>
</blockquote></td>
<td><blockquote>
<p>techmarket@gmail.com</p>
</blockquote></td>
</tr>
<tr>
<td><blockquote>
<p>To</p>
</blockquote></td>
<td><blockquote>
<p>[email]</p>
</blockquote></td>
</tr>
<tr>
<td><blockquote>
<p>Cc</p>
</blockquote></td>
<td><blockquote>
<p>N/A</p>
</blockquote></td>
</tr>
<tr>
<td><blockquote>
<p>Subject</p>
</blockquote></td>
<td><blockquote>
<p>Get [Subject] of “Email Template” item of which [Keyword] = “Sign
Up”</p>
</blockquote></td>
</tr>
<tr>
<td><blockquote>
<p>Body</p>
</blockquote></td>
<td><blockquote>
<p>Get [Body] of “Email Template” item of which [Keyword] = “Sign
Up”</p>
</blockquote></td>
</tr>
</tbody>
</table>
<ul>
<li><p>Following is sample email content:</p></li>
</ul>
<table>
<colgroup>
<col style="width: 25%" />
<col style="width: 74%" />
</colgroup>
<tbody>
<tr>
<td><blockquote>
<p>Subject</p>
</blockquote></td>
<td><blockquote>
<p>"Verify Registration Tech Market Account"</p>
</blockquote></td>
</tr>
<tr>
<td><blockquote>
<p>Body</p>
</blockquote></td>
<td><blockquote>
<p>[Body] = “Hello,”</p>
<p>[Body] = [Body] + 2 new lines</p>
<p>[Body] = [Body] + “Follow this link to verify your email address to
finish your registration step.”</p>
<p>[Body] = [Body] + 2 new lines</p>
<p>[Body] = [Body] + &lt;&lt;Link to verify email&gt;&gt;</p>
<p>[Body] = [Body] + "<mark>If you didn’t ask to verify this address,
you can ignore this email.</mark>"</p>
<p>[Body] = [Body] + 2 new lines</p>
<p>[Body] = [Body] + "<mark>Thanks.</mark>"</p>
<p>[Body] = [Body] + 2 new lines</p>
<p>[Body] = [Body] + "The <mark>Tech Market team</mark>"</p>
</blockquote></td>
</tr>
</tbody>
</table></td>
</tr>
<tr>
<td style="text-align: center;"><em>(5)</em></td>
<td style="text-align: center;"><em>BR6</em></td>
<td style="text-align: center;"><p><strong>Message Rules:</strong></p>
<ul>
<li><p>The system shows success message MSG 28</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(4)</em></td>
<td style="text-align: center;"><em>BR7</em></td>
<td style="text-align: center;"><p><strong>Message Rules:</strong></p>
<ul>
<li><p>The system shows success message MSG 29</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(6)</em></td>
<td style="text-align: center;"><em>BR8</em></td>
<td style="text-align: center;"><p><strong>Redirect Rules:</strong></p>
<ul>
<li><p>The system redirects to the home page.</p></li>
</ul></td>
</tr>
</tbody>
</table>

### UC3: Forgot Password

<table>
<colgroup>
<col style="width: 21%" />
<col style="width: 78%" />
</colgroup>
<tbody>
<tr>
<td><strong>Name</strong></td>
<td><strong>Forgot Password</strong></td>
</tr>
<tr>
<td><strong>Description</strong></td>
<td>This use case describes the process by which users reset their
password when they forgot it.</td>
</tr>
<tr>
<td><strong>Actor</strong></td>
<td>User</td>
</tr>
<tr>
<td><strong>Trigger</strong></td>
<td><ul>
<li><p>When the user clicks on the “Forgot password” button.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Pre-condition</strong></td>
<td><ul>
<li><p>The user is not logged in to the system.</p></li>
<li><p>The user is in the sign in page.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Post-condition</strong></td>
<td><ul>
<li><p>Password has been changed.</p></li>
<li><p>The user is redirected to the home page.</p></li>
</ul></td>
</tr>
</tbody>
</table>

#### Activities Flow

<img src="media/image3.png" style="width:6.69272in;height:7.25in" />

#### 

#### 

#### 

#### 

#### Business Rules

<table>
<colgroup>
<col style="width: 14%" />
<col style="width: 13%" />
<col style="width: 71%" />
</colgroup>
<tbody>
<tr>
<td style="text-align: center;"><strong>Activity</strong></td>
<td style="text-align: center;"><strong>BR Code</strong></td>
<td style="text-align: center;"><strong>Description</strong></td>
</tr>
<tr>
<td style="text-align: center;"><em>(2)</em></td>
<td style="text-align: center;"><em>BR9</em></td>
<td style="text-align: center;"><p><strong>Redirect Rules:</strong></p>
<ul>
<li><p>The system redirects to the forgot password page (refer to
“”Forgot Password Form” in “List description” file).</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(4)</em></td>
<td style="text-align: center;"><em>BR10</em></td>
<td style="text-align: center;"><p><strong>Validate Rules:</strong></p>
<ul>
<li><p>The system will receive [email] or [phoneNumber].</p></li>
<li><p>If [email] or [phoneNumber] is null or blank, then return
400-BAD_REQUEST error with error message MSG2.</p></li>
<li><p>If
pattern.compile(‘^(84|0[3|5|7|8|9])+([0-9]{8})$’).notMatch([phoneNumber])
or then returns 400-BAD_REQUEST error with error message MSG 30</p></li>
<li><p>If
pattern.compile(‘^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$’).notMatch([email])
then returns 400-BAD_REQUEST error with error message MSG31.</p></li>
<li><p>[user] = User Repository findByPhoneOrEmail([phoneNumber],
[email]) (call findByPhoneOrEmail() function)</p></li>
<li><p>If [user] == null then returns 400-BAD_REQUEST error with error
message MSG32.</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(5)</em></td>
<td style="text-align: center;"><em>BR11</em></td>
<td style="text-align: center;"><p><strong>Generate Link
Rules:</strong></p>
<ul>
<li><p>[link.expired_in] = Date.now().plus(10, MINUTES).</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(6)</em></td>
<td style="text-align: center;"><em>BR12</em></td>
<td style="text-align: center;"><p><strong>Send Link Rules:</strong></p>
<p><strong>Email Templates:</strong></p>
<blockquote>
<p>❖ Send mail to user change password or message through zalo as the
template below<strong>:</strong></p>
</blockquote>
<table>
<colgroup>
<col style="width: 25%" />
<col style="width: 74%" />
</colgroup>
<tbody>
<tr>
<td><blockquote>
<p>From</p>
</blockquote></td>
<td><blockquote>
<p>techmarket@gmail.com</p>
</blockquote></td>
</tr>
<tr>
<td><blockquote>
<p>To</p>
</blockquote></td>
<td><blockquote>
<p>[user.email]</p>
</blockquote></td>
</tr>
<tr>
<td><blockquote>
<p>Cc</p>
</blockquote></td>
<td><blockquote>
<p>N/A</p>
</blockquote></td>
</tr>
<tr>
<td><blockquote>
<p>Subject</p>
</blockquote></td>
<td><blockquote>
<p>Get [Subject] of “Email Template” item of which [Keyword] = “Forgot
Password”</p>
</blockquote></td>
</tr>
<tr>
<td><blockquote>
<p>Body</p>
</blockquote></td>
<td><blockquote>
<p>Get [Body] of “Email Template” item of which [Keyword] = “Forgot
Password”</p>
</blockquote></td>
</tr>
</tbody>
</table>
<blockquote>
<p>Following is sample email content:</p>
</blockquote>
<table>
<colgroup>
<col style="width: 25%" />
<col style="width: 74%" />
</colgroup>
<tbody>
<tr>
<td><blockquote>
<p>Subject</p>
</blockquote></td>
<td><blockquote>
<p>"Reset Password"</p>
</blockquote></td>
</tr>
<tr>
<td><blockquote>
<p>Body</p>
</blockquote></td>
<td><blockquote>
<p>[Body] = “Hello,”</p>
<p>[Body] = [Body] + 2 new lines</p>
<p>[Body] = [Body] + “You have requested to reset the password of your
TECH MARKET account.”</p>
<p>[Body] = [Body] + 2 new lines</p>
<p>[Body] = [Body] + "<mark>Please click the link to change your
password:</mark> "</p>
<p>[Body] = [Body] + 2 new lines</p>
<p>[Body] = [Body] + [link]</p>
<p>[Body] = [Body] + 2 new lines</p>
<p>[Body] = [Body] + "If you didn’t ask to reset your password, you can
ignore this email."</p>
<p>[Body] = [Body] + "<mark>Thanks.</mark>"</p>
<p>[Body] = [Body] + 2 new lines</p>
<p>[Body] = [Body] + "The <mark>TechMarket team</mark>"</p>
</blockquote></td>
</tr>
</tbody>
</table></td>
</tr>
<tr>
<td style="text-align: center;"><em>(8)</em></td>
<td style="text-align: center;"><em>BR13</em></td>
<td style="text-align: center;"><p><strong>Validate Rules:</strong></p>
<ul>
<li><p>If [link.expired_in].isAfter(Date,now()) then the system
redirects to the Change password page.</p></li>
</ul>
<blockquote>
<p>else the system will show an error message MSG 33.</p>
</blockquote></td>
</tr>
<tr>
<td style="text-align: center;"><em>(12)</em></td>
<td style="text-align: center;"><em>BR14</em></td>
<td style="text-align: center;"><p><strong>Validate Rules:</strong></p>
<ul>
<li><p>The system will receive [password].</p></li>
<li><p>If [password] is null or blank, then return 400-BAD_REQUEST error
with error message MSG2.</p></li>
<li><p>If
pattern.compile(“"^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[@#$%^&amp;+=!])(?=\\S+$).{8,}$"”).notMatch([password])
then the system shows an error message MSG 25.</p></li>
<li><p>If hash([password]) == [user.password] then the system shows an
error message MSG 34.</p></li>
</ul>
<blockquote>
<p>else [user.password] = hash([password])</p>
</blockquote></td>
</tr>
<tr>
<td style="text-align: center;"><em>(14)</em></td>
<td style="text-align: center;"><em>BR15</em></td>
<td style="text-align: center;"><p><strong>Redirect rules:</strong></p>
<ul>
<li><p>The system redirects to the sign in page.</p></li>
</ul></td>
</tr>
</tbody>
</table>

### UC4: Leave a feedback

<table>
<colgroup>
<col style="width: 21%" />
<col style="width: 78%" />
</colgroup>
<tbody>
<tr>
<td><strong>Name</strong></td>
<td><strong>Leave a feedback</strong></td>
</tr>
<tr>
<td><strong>Description</strong></td>
<td>This use case describes how users can leave feedback.</td>
</tr>
<tr>
<td><strong>Actor</strong></td>
<td>User</td>
</tr>
<tr>
<td><strong>Trigger</strong></td>
<td><ul>
<li><p>When the admin clicks on the “Feedback” button.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Pre-condition</strong></td>
<td><ul>
<li><p>The user are access to the TechMarket website</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Post-condition</strong></td>
<td><ul>
<li><p>The feedback are created</p></li>
</ul></td>
</tr>
</tbody>
</table>

#### Activities Flow

<img src="media/image4.png" style="width:6.69792in;height:3.69206in" />

#### Business Rules

<table>
<colgroup>
<col style="width: 14%" />
<col style="width: 13%" />
<col style="width: 71%" />
</colgroup>
<tbody>
<tr>
<td style="text-align: center;"><strong>Activity</strong></td>
<td style="text-align: center;"><strong>BR Code</strong></td>
<td style="text-align: center;"><strong>Description</strong></td>
</tr>
<tr>
<td style="text-align: center;"><em>(2)</em></td>
<td style="text-align: center;"><em>BR16</em></td>
<td style="text-align: center;"><p><strong>Feedback Form
Rules:</strong></p>
<ul>
<li><p>The system loads the “Leave a feedback” page (refer to “”Leave
Feedback Form” in “List description” file).</p></li>
<li><p>The form includes the following information fields:</p>
<ul>
<li><p>[feedback_category] Feedback category:</p>
<ul>
<li><p>Faulty feature</p></li>
<li><p>Request new feature</p></li>
<li><p>Other</p></li>
</ul></li>
<li><p>[comment]</p></li>
<li><p>[proof] ( image/video )</p></li>
</ul></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(5)</em></td>
<td style="text-align: center;"><em>BR17</em></td>
<td style="text-align: center;"><p><strong>Creating Rules:</strong></p>
<p>When the user clicks on “Save”, the system will prompt a confirmation
message (Refer to MSG 1). If user chooses Cancel, the system does
nothing; else, the system will save inputted information and update the
item as the following:</p>
<ul>
<li><p>The system checks the items [feedback_category],
[comment]</p></li>
</ul>
<ul>
<li><p>If any entries are empty, the system shows an error message MSG
2.</p></li>
<li><p>The feedback will be saved with the items: [feedback_category],
[comment], [proof] (can be null ), [status] = PENDING</p></li>
</ul></td>
</tr>
</tbody>
</table>

### UC5: Create post

<table>
<colgroup>
<col style="width: 21%" />
<col style="width: 78%" />
</colgroup>
<tbody>
<tr>
<td><strong>Name</strong></td>
<td><strong>Create post</strong></td>
</tr>
<tr>
<td><strong>Description</strong></td>
<td>This use case allows Sellers to create a post that contains their
product information.</td>
</tr>
<tr>
<td><strong>Actor</strong></td>
<td>User</td>
</tr>
<tr>
<td><strong>Trigger</strong></td>
<td><ul>
<li><p>When the user clicks on the “Create post” button.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Pre-condition</strong></td>
<td><ul>
<li><p>The user is logged in to the system.</p></li>
<li><p>The user is in the create post page.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Post-condition</strong></td>
<td><ul>
<li><p>The posts have been created.</p></li>
</ul></td>
</tr>
</tbody>
</table>

#### Activities Flow

<img src="media/image5.png" style="width:6.68889in;height:3.96319in"
alt="A screenshot of a computer Description automatically generated" />

*Figure 1: Activities Flow*

#### Business Rules

<table>
<colgroup>
<col style="width: 14%" />
<col style="width: 13%" />
<col style="width: 71%" />
</colgroup>
<tbody>
<tr>
<td style="text-align: center;"><strong>Activity</strong></td>
<td style="text-align: center;"><strong>BR Code</strong></td>
<td style="text-align: center;"><strong>Description</strong></td>
</tr>
<tr>
<td style="text-align: center;"><em>(2)</em></td>
<td style="text-align: center;"><em>BR18</em></td>
<td style="text-align: center;"><p><strong>Loading Screen
Rules:</strong></p>
<ul>
<li><p>The system loads the “Create post” screen (refer to “”Create Post
Form” in “List description” file).</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(5)</em></td>
<td style="text-align: center;"><em>BR19</em></td>
<td style="text-align: center;"><p><strong>Creating Rules:</strong></p>
<p>When the user clicks on “Save”, the system will prompt a confirmation
message (Refer to MSG 1). If user chooses Cancel, the system does
nothing; else, the system will save inputted information and update the
item as the following:</p>
<ul>
<li><p>The client concat values from [province], [district] and [ward]
to [area].</p></li>
<li><p>The system checks the items [images], [product], [price],
[title], [description], [area].</p></li>
</ul>
<ul>
<li><p>If any entries are empty, the system shows an error message MSG
2.</p></li>
<li><p>If size of any in [images] &gt; 8.MB then system shows error
message MSG 11</p></li>
<li><p>If [product] does not exist then the system shows error message
MSG 12.</p></li>
<li><p>If [price] &lt; 0 then the system shows error message MSG
13.</p></li>
<li><p>If [description].length &lt; 50 then the system shows error
message MSG 14.</p></li>
</ul>
<ul>
<li><p>[user] = &lt;&lt;current user id retrieved from
jwt&gt;&gt;</p></li>
<li><p>[createdDate] = &lt;&lt;current date time&gt;&gt;</p></li>
<li><p>[status] = ‘CREATED’. When a post is in this state, no one except
the owner and Administrator can see and interact with it</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(6)</em></td>
<td style="text-align: center;"><em>BR20</em></td>
<td style="text-align: center;"><p><strong>Message Rules:</strong></p>
<ul>
<li><p>The system shows success message MSG 3</p></li>
</ul></td>
</tr>
</tbody>
</table>

### UC6: Update post

<table>
<colgroup>
<col style="width: 21%" />
<col style="width: 78%" />
</colgroup>
<tbody>
<tr>
<td><strong>Name</strong></td>
<td><strong>Update post</strong></td>
</tr>
<tr>
<td><strong>Description</strong></td>
<td>This use case describes how user can update a post</td>
</tr>
<tr>
<td><strong>Actor</strong></td>
<td>User</td>
</tr>
<tr>
<td><strong>Trigger</strong></td>
<td><ul>
<li><p>When the user clicks on the “Save” button.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Pre-condition</strong></td>
<td><ul>
<li><p>The user is logged in to the system.</p></li>
<li><p>The user has created this post.</p></li>
<li><p>The user in the edit post page.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Post-condition</strong></td>
<td><ul>
<li><p>The posts have been updated.</p></li>
<li><p>The post is in ‘Waiting’ status</p></li>
</ul></td>
</tr>
</tbody>
</table>

#### Activities Flow

<img src="media/image6.png" style="width:6.69272in;height:5.51389in" />

#### Business Rules

<table>
<colgroup>
<col style="width: 14%" />
<col style="width: 13%" />
<col style="width: 71%" />
</colgroup>
<tbody>
<tr>
<td style="text-align: center;"><strong>Activity</strong></td>
<td style="text-align: center;"><strong>BR Code</strong></td>
<td style="text-align: center;"><strong>Description</strong></td>
</tr>
<tr>
<td style="text-align: center;"><em>(2)</em></td>
<td style="text-align: center;"><em>BR21</em></td>
<td style="text-align: center;"><p><strong>Loading Screen
Rules:</strong></p>
<ul>
<li><p>The system loads the “Update post” screen (refer to “”Update Post
Form” in “List description” file).</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(5)</em></td>
<td style="text-align: center;"><em>BR22</em></td>
<td style="text-align: center;"><p><strong>Validate Rules:</strong></p>
<p>When the user clicks on “Save”, the system will prompt a confirmation
message (Refer to MSG 1). If user chooses Cancel, the system does
nothing; else, the system will save inputted information and update the
item as the following:</p>
<ul>
<li><p>The system checks the items [images], [product], [price],
[title], [description], [area].</p></li>
</ul>
<ul>
<li><p>If any entries are empty, the system shows an error message MSG
2.</p></li>
<li><p>If size of any in [images] &gt; 8.MB then system shows error
message MSG 11</p></li>
<li><p>If [product] does not exist then the system shows error message
MSG 12.</p></li>
<li><p>If [price] &lt; 0 then the system shows error message MSG
13.</p></li>
<li><p>If [description].length &lt; 50 then the system shows error
message MSG 14.</p></li>
</ul>
<ul>
<li><p>[user] = &lt;&lt;current user id retrieved from
jwt&gt;&gt;</p></li>
<li><p>[updatedDate] = &lt;&lt;current date time&gt;&gt;</p></li>
<li><p>[status] = ‘PENDING’. When a post is in this state, no one except
the owner and Administrator can see and interact with it</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(7)</em></td>
<td style="text-align: center;"><em>BR23</em></td>
<td style="text-align: center;"><p><strong>Message Rules</strong></p>
<ul>
<li><p>The system shows success message MSG 16</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(8)</em></td>
<td style="text-align: center;"><em>BR24</em></td>
<td style="text-align: center;"><p><strong>Message Rules:</strong></p>
<ul>
<li><p>The system shows success message MSG 17</p></li>
</ul></td>
</tr>
</tbody>
</table>

### UC7: Delete post

<table>
<colgroup>
<col style="width: 21%" />
<col style="width: 78%" />
</colgroup>
<tbody>
<tr>
<td><strong>Name</strong></td>
<td><strong>Delete post</strong></td>
</tr>
<tr>
<td><strong>Description</strong></td>
<td>This use case describes how a user can delete a post.</td>
</tr>
<tr>
<td><strong>Actor</strong></td>
<td>User</td>
</tr>
<tr>
<td><strong>Trigger</strong></td>
<td><ul>
<li><p>When the user clicks on the “Delete post” button.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Pre-condition</strong></td>
<td><ul>
<li><p>The user is logged in to the system.</p></li>
<li><p>The user has created this post.</p></li>
<li><p>The user in the edit post page.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Post-condition</strong></td>
<td><ul>
<li><p>The posts have been deleted.</p></li>
</ul></td>
</tr>
</tbody>
</table>

#### Activities Flow

<img src="media/image7.png" style="width:6.69272in;height:4.61111in" />

#### 

#### Business Rules

<table>
<colgroup>
<col style="width: 14%" />
<col style="width: 13%" />
<col style="width: 71%" />
</colgroup>
<tbody>
<tr>
<td style="text-align: center;"><strong>Activity</strong></td>
<td style="text-align: center;"><strong>BR Code</strong></td>
<td style="text-align: center;"><strong>Description</strong></td>
</tr>
<tr>
<td style="text-align: center;"><em>(2)</em></td>
<td style="text-align: center;"><em>BR25</em></td>
<td style="text-align: center;"><p><strong>Loading Modal
Rules:</strong></p>
<ul>
<li><p>The system loads the “Delete confirmation” modal.</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(4)</em></td>
<td style="text-align: center;"><em>BR26</em></td>
<td style="text-align: center;"><p><strong>Checking Rules:</strong></p>
<ul>
<li><p>If [postId] does not exist, the system shows an error message MSG
18 else [post] = Post Repository find by [postId] (call findById()
function)</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(5)</em></td>
<td style="text-align: center;"><em>BR27</em></td>
<td style="text-align: center;"><p><strong>Delete Rules:</strong></p>
<ul>
<li><p>If [post.order] == null then Post Repository delete by [post.id]
(call deleteById() function) else the system show error message MSG
19</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(6)</em></td>
<td style="text-align: center;"><em>BR28</em></td>
<td style="text-align: center;"><p><strong>Message Rules:</strong></p>
<ul>
<li><p>The system shows success message MSG 20</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(7)</em></td>
<td style="text-align: center;"><em>BR29</em></td>
<td style="text-align: center;"><p><strong>Message Rules:</strong></p>
<ul>
<li><p>The system shows error message MSG 21</p></li>
</ul></td>
</tr>
</tbody>
</table>

### 

### UC8: Guarantee payment

<table>
<colgroup>
<col style="width: 21%" />
<col style="width: 78%" />
</colgroup>
<tbody>
<tr>
<td><strong>Name</strong></td>
<td><strong>Guarantee payment</strong></td>
</tr>
<tr>
<td><strong>Description</strong></td>
<td>This use case describes how users can use guaranteed payment
functionality for their posts.</td>
</tr>
<tr>
<td><strong>Actor</strong></td>
<td>Seller</td>
</tr>
<tr>
<td><strong>Trigger</strong></td>
<td><ul>
<li><p>When the Seller clicks the button.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Pre-condition</strong></td>
<td><ul>
<li><p>The Seller logs into the system.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Post-condition</strong></td>
<td><ul>
<li><p>The post is updated to the ‘Guarantee’ plan.</p></li>
</ul></td>
</tr>
</tbody>
</table>

#### Activities Flow

<img src="media/image8.png" style="width:6.69272in;height:3.84722in" />

#### Business Rules

<table>
<colgroup>
<col style="width: 14%" />
<col style="width: 12%" />
<col style="width: 73%" />
</colgroup>
<tbody>
<tr>
<td style="text-align: center;"><strong>Activity</strong></td>
<td style="text-align: center;"><strong>BR Code</strong></td>
<td style="text-align: center;"><strong>Description</strong></td>
</tr>
<tr>
<td style="text-align: center;"><em>(2)</em></td>
<td style="text-align: center;"><em>BR30</em></td>
<td style="text-align: center;"><p><strong>Loading Screen
Rules:</strong></p>
<ul>
<li><p>The system loads the post details screen (refer to “Seller/Post
Details” in “List description” file).</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(5)</em></td>
<td style="text-align: center;"><em>BR31</em></td>
<td style="text-align: center;"><p><strong>Check Sales Wallet
Rules:</strong></p>
<ul>
<li><p>If ([user.sales_wallet] != null) then the system return response
with status code 200 and show confirm message MSG 1</p></li>
</ul>
<blockquote>
<p>else the system return response with status code 400</p>
</blockquote></td>
</tr>
<tr>
<td style="text-align: center;"><em>(5.1)</em></td>
<td style="text-align: center;"><em>BR32</em></td>
<td style="text-align: center;"><p><strong>Update post
rules:</strong></p>
<ul>
<li><p>[post.label] = ‘GUARANTEE’</p></li>
<li><p>The system shows message MSG 43</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(5.2)</em></td>
<td style="text-align: center;"><em>BR33</em></td>
<td style="text-align: center;"><p><strong>Request link sales wallet
Rules:</strong></p>
<ul>
<li><p>The system shows message MSG 44</p></li>
</ul></td>
</tr>
</tbody>
</table>

### 

### UC9: Link sales wallet

<table>
<colgroup>
<col style="width: 21%" />
<col style="width: 78%" />
</colgroup>
<tbody>
<tr>
<td><strong>Name</strong></td>
<td><strong>Link sales wallet</strong></td>
</tr>
<tr>
<td><strong>Description</strong></td>
<td>This use case describes how a user can link to their merchant
wallet.</td>
</tr>
<tr>
<td><strong>Actor</strong></td>
<td>Seller</td>
</tr>
<tr>
<td><strong>Trigger</strong></td>
<td><ul>
<li><p>When the Seller clicks the “Link sales wallet” button.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Pre-condition</strong></td>
<td><ul>
<li><p>The Seller logs into the system.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Post-condition</strong></td>
<td><ul>
<li><p>This Seller ‘s sales wallet is activated.</p></li>
</ul></td>
</tr>
</tbody>
</table>

#### Activities Flow

<img src="media/image9.png" style="width:6.69272in;height:4.93056in" />

#### Business Rules

<table>
<colgroup>
<col style="width: 14%" />
<col style="width: 12%" />
<col style="width: 73%" />
</colgroup>
<tbody>
<tr>
<td style="text-align: center;"><strong>Activity</strong></td>
<td style="text-align: center;"><strong>BR Code</strong></td>
<td style="text-align: center;"><strong>Description</strong></td>
</tr>
<tr>
<td style="text-align: center;"><em>(2)</em></td>
<td style="text-align: center;"><em>BR34</em></td>
<td style="text-align: center;"><p><strong>Loading Screen
Rules:</strong></p>
<ul>
<li><p>The system loads the “Wallet link” screen (refer to “Wallet Link”
in “List description” file).</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(3)</em></td>
<td style="text-align: center;"><em>BR35</em></td>
<td style="text-align: center;"><p><strong>Select wallet
Rules:</strong></p>
<ul>
<li><p>The user chooses one of the following wallets:</p>
<ul>
<li><p>Momo</p></li>
<li><p>Paypoo</p></li>
</ul></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(7)</em></td>
<td style="text-align: center;"><em>BR36</em></td>
<td style="text-align: center;"><p><strong>Phone number verification
rules:</strong></p>
<ul>
<li><p>The system use the abstract API phone validator to verify phone
numbers with default country code is ‘VN’</p></li>
<li><p>If [phone] is valid then the abstract API return response
contains [valid] = true</p></li>
</ul>
<p>else the abstract API return response contains [valid] =
false</p></td>
</tr>
<tr>
<td style="text-align: center;"><em>(7.1)</em></td>
<td style="text-align: center;"><em>BR37</em></td>
<td style="text-align: center;"><p><strong>Sending OTP Code
Rules:</strong></p>
<ul>
<li><p>The system generate an OTP code to verify user own phone number
with [expire] = 60000 ( mean 1 minutes )</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(7.2)</em></td>
<td style="text-align: center;"><em>BR38</em></td>
<td style="text-align: center;"><p><strong>Message Rules:</strong></p>
<ul>
<li><p>The system show message MSG 45</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(9)</em></td>
<td style="text-align: center;"><em>BR39</em></td>
<td style="text-align: center;"><p><strong>Checking OTP
Rules:</strong></p>
<ul>
<li><p>If [OTP_code] is match then the system return response with
status code 200</p></li>
<li><p>else the system return response with status code 400</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(9.1)</em></td>
<td style="text-align: center;"><em>BR40</em></td>
<td style="text-align: center;"><p><strong>Update sales wallet
rules:</strong></p>
<ul>
<li><p>If response.status_code == 200 then</p></li>
</ul>
<blockquote>
<p>[user.sales_wallet] = {</p>
<p>“vendor” : &lt;&lt;selected wallet&gt;&gt; (ex: “Momo”),</p>
<p>“phone” : &lt;&lt;encrypted phone number&gt;&gt;</p>
<p>}</p>
</blockquote></td>
</tr>
<tr>
<td style="text-align: center;"><em>(9.2)</em></td>
<td style="text-align: center;"><em>BR41</em></td>
<td style="text-align: center;"><p><strong>Message rules:</strong></p>
<ul>
<li><p>The system show message MSG 46</p></li>
</ul></td>
</tr>
</tbody>
</table>

### 

### 

### UC10: Use post push service

<table>
<colgroup>
<col style="width: 21%" />
<col style="width: 78%" />
</colgroup>
<tbody>
<tr>
<td><strong>Name</strong></td>
<td><strong>Use post push service</strong></td>
</tr>
<tr>
<td><strong>Description</strong></td>
<td>This use case describes how a user can use the post push service,
increase product sales.</td>
</tr>
<tr>
<td><strong>Actor</strong></td>
<td>Seller</td>
</tr>
<tr>
<td><strong>Trigger</strong></td>
<td><ul>
<li><p>When the Seller clicks the 'Push to top’ button.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Pre-condition</strong></td>
<td><ul>
<li><p>The Seller logs into the system.</p></li>
<li><p>The post has been created, approved by Admin and not hidden
.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Post-condition</strong></td>
<td><ul>
<li><p>The post is pushed to the top of the page.</p></li>
</ul></td>
</tr>
</tbody>
</table>

#### 

#### Activities Flow

<img src="media/image10.png" style="width:6.22041in;height:6.40891in" />

#### Business Rules

<table>
<colgroup>
<col style="width: 14%" />
<col style="width: 12%" />
<col style="width: 73%" />
</colgroup>
<tbody>
<tr>
<td style="text-align: center;"><strong>Activity</strong></td>
<td style="text-align: center;"><strong>BR Code</strong></td>
<td style="text-align: center;"><strong>Description</strong></td>
</tr>
<tr>
<td style="text-align: center;"><em>(3)</em></td>
<td style="text-align: center;"><em>BR42</em></td>
<td style="text-align: center;"><p><strong>Loading Screen
Rules:</strong></p>
<ul>
<li><p>The system loads the “Post push plan” screen” (refer to “Post
Push Plan” in “List description” file).</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(4)</em></td>
<td style="text-align: center;"><em>BR43</em></td>
<td style="text-align: center;"><p><strong>Post push plan
Rules:</strong></p>
<ul>
<li><p>The system shows service plan list comes with price (for each
push):</p></li>
</ul>
<ul>
<li><p>Now</p></li>
<li><p>3 days</p></li>
<li><p>7 days</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(5)</em></td>
<td style="text-align: center;"><em>BR44</em></td>
<td style="text-align: center;"><p><strong>Post push time frame
Rules:</strong></p>
<ul>
<li><p>The system shows service time frame list:</p></li>
</ul>
<ul>
<li><p>8h00 - 9h00</p></li>
<li><p>9h00 - 10h00</p></li>
<li><p>10h00 - 11h00</p></li>
<li><p>13h00 - 14h00</p></li>
<li><p>14h00 - 15h00</p></li>
<li><p>15h00 - 16h00</p></li>
<li><p>16h00 - 17h00</p></li>
<li><p>18h00 - 19h00</p></li>
<li><p>19h00 - 20h00</p></li>
<li><p>20h00 - 21h00</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(7)</em></td>
<td style="text-align: center;"><em>BR45</em></td>
<td style="text-align: center;"><p><strong>Loading Screen
Rules:</strong></p>
<ul>
<li><p>The system loads the “Post push checkout” screen (refer to “Post
Push Checkout” in “List description” file).</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(8)</em></td>
<td style="text-align: center;"><em>BR46</em></td>
<td style="text-align: center;"><p><strong>Select payment method
Rules:</strong></p>
<ul>
<li><p>The user selects the following available payment
methods:</p></li>
</ul>
<ul>
<li><p>Cash on delivery (COD)</p></li>
<li><p>Payment via visa/mastercard</p></li>
<li><p>Payment via Momo e-wallet</p></li>
<li><p>Payment via PayPoo</p></li>
<li><p>Payment via ZaloPay</p></li>
<li><p>Payment via VNPAY</p></li>
<li><p>Payment via Paypal payment gateway</p></li>
<li><p>Payment via internet banking . A few affiliate banks are
available like: ABBank, ACB,AgriBank, Bac A Bank, Bảo Việt Bank, BIDV,
Đông á bank, Eximbank, GPBank, HDBank, Liên Việt Bank, MB Bank, Nam Á
Bank, NCB, OCB, OCEAN BANK, Sacombank, SCB, SeaBank, SHB, TechcomBank,
TPBank, VIB, VietABank, Vietcombank, Vietinbank, VPBank</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(10)</em></td>
<td style="text-align: center;"><em>BR47</em></td>
<td style="text-align: center;"><p><strong>Payment process
rules:</strong></p>
<p>When the user clicks on “Pay”, the system will prompt a confirmation
message (Refer to MSG 1). If user chooses Cancel, the system does
nothing; else, the system will save inputted information and update the
item as the following:</p>
<ul>
<li><p>The system checks the items [post_id], [Rules], [Time
frames].</p></li>
<li><p>If any entries are empty, the system shows an error message MSG
2.</p></li>
<li><p>[Push Plan.post] = Post repository find by id [post_id] (call
findById() function)</p></li>
<li><p>[Push Plan.rules] = [Rules]</p></li>
<li><p>[Push Plan.timeFrames] = [Time frames]</p></li>
<li><p>Save [Push Plan] to database (call save() function)</p></li>
<li><p>The system will process payment like UC14</p></li>
</ul></td>
</tr>
</tbody>
</table>

### UC11: Confirm sales order

<table>
<colgroup>
<col style="width: 21%" />
<col style="width: 78%" />
</colgroup>
<tbody>
<tr>
<td><strong>Name</strong></td>
<td><strong>Confirm sales order</strong></td>
</tr>
<tr>
<td><strong>Description</strong></td>
<td>This use case describes how sellers can confirm the order of
buyers.</td>
</tr>
<tr>
<td><strong>Actor</strong></td>
<td>Seller</td>
</tr>
<tr>
<td><strong>Trigger</strong></td>
<td><ul>
<li><p>When the user clicks on the “Confirm” button.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Pre-condition</strong></td>
<td><ul>
<li><p>The buyer is logged in to the system.</p></li>
<li><p>The user has placed the order successfully.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Post-condition</strong></td>
<td><ul>
<li><p>The order status has been changed to ’CONFIRMED’</p></li>
</ul></td>
</tr>
</tbody>
</table>

#### Activities Flow

<img src="media/image11.png" style="width:6.69272in;height:4.81944in" />

#### Business Rules

<table>
<colgroup>
<col style="width: 14%" />
<col style="width: 12%" />
<col style="width: 73%" />
</colgroup>
<tbody>
<tr>
<td style="text-align: center;"><strong>Activity</strong></td>
<td style="text-align: center;"><strong>BR Code</strong></td>
<td style="text-align: center;"><strong>Description</strong></td>
</tr>
<tr>
<td style="text-align: center;"><em>(1)</em></td>
<td style="text-align: center;"><em>BR48</em></td>
<td style="text-align: center;"><p><strong>Loading Screen
Rules:</strong></p>
<ul>
<li><p>The system loads the “Order Details” screen (refer to
“Seller/Order Details” in the “List description” file).</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(7)</em></td>
<td style="text-align: center;"><em>BR49</em></td>
<td style="text-align: center;"><p><strong>Confirm rules:</strong></p>
<ul>
<li><p>The system extract the [orderId] from the request</p></li>
<li><p>If [orderId] == null then the system returns error response with
status code 400 BAD_REQUEST</p></li>
<li><p>If [order] = Order repository find by id ([orderId]) == null then
the system returns error response with status code 400
BAD_REQUEST</p></li>
<li><p>If [order.status] != ‘CREATED’ then the systems returns error
response with status code 400 BAD_REQUEST</p></li>
<li><p>[order.status] = ‘CONFIRMED’</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(8)</em></td>
<td style="text-align: center;"><em>BR50</em></td>
<td style="text-align: center;"><p><strong>Message rules:</strong></p>
<ul>
<li><p>The system shows message MSG 40.</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(9)</em></td>
<td style="text-align: center;"><em>BR51</em></td>
<td style="text-align: center;"><p><strong>Send message
rules:</strong></p>
<ul>
<li><p>The system sends a notification to the buyer’s dialog box
according to the following template:</p></li>
<li><p>[Seller name] + ' has successfully confirmed the order with id' +
[Order id].</p></li>
</ul></td>
</tr>
</tbody>
</table>

### UC12: Place order

<table>
<colgroup>
<col style="width: 21%" />
<col style="width: 78%" />
</colgroup>
<tbody>
<tr>
<td><strong>Name</strong></td>
<td><strong>Place order</strong></td>
</tr>
<tr>
<td><strong>Description</strong></td>
<td>This use case allows the Buyer to place orders.</td>
</tr>
<tr>
<td><strong>Actor</strong></td>
<td>Buyer</td>
</tr>
<tr>
<td><strong>Trigger</strong></td>
<td><ul>
<li><p>When the user clicks on the “Buy now” button in the post details
screen.</p></li>
<li><p>Otherwise, user clicks on “Buy” button in cart screen</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Pre-condition</strong></td>
<td><ul>
<li><p>The user is logged in to the system.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Post-condition</strong></td>
<td><ul>
<li><p>The order has been created.</p></li>
<li><p>In case the user chooses to pay online, the payment will be
transferred to TechMarket's account for safekeeping.</p></li>
</ul></td>
</tr>
</tbody>
</table>

#### Activities Flow

<img src="media/image12.png" style="width:6.68889in;height:5.05278in"
alt="A screenshot of a computer Description automatically generated" />

#### Business Rules

<table>
<colgroup>
<col style="width: 14%" />
<col style="width: 14%" />
<col style="width: 70%" />
</colgroup>
<tbody>
<tr>
<td style="text-align: center;"><strong>Activity</strong></td>
<td style="text-align: center;"><strong>BR Code</strong></td>
<td style="text-align: center;"><strong>Description</strong></td>
</tr>
<tr>
<td style="text-align: center;"><em>(2)</em></td>
<td style="text-align: center;"><em>BR52</em></td>
<td style="text-align: center;"><p><strong>Loading Screen
Rules:</strong></p>
<ul>
<li><p>The system loads the “Check out” screen (refer to “Checkout” in
the “List description” file).</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(4.1)</em></td>
<td style="text-align: center;"><em>BR53</em></td>
<td style="text-align: center;"><p><strong>Address enter
Rules:</strong></p>
<ul>
<li><p>The user selects an existing address previously saved in the
account or enters a new address.</p></li>
<li><p>When the user clicks on “Save”, the system will prompt a
confirmation message (Refer to MSG 1). If user chooses Cancel, the
system does nothing; else, the system will save inputted information and
update the item as the following:</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(4.2)</em></td>
<td style="text-align: center;"><em>BR54</em></td>
<td style="text-align: center;"><p><strong>Consignee information enter
Rules:</strong></p>
<ul>
<li><p>The user enters consignee information, including [Name], [Phone
number].</p></li>
</ul>
<ul>
<li><p>Additionally, users can confirm information available in the
account (if any)</p></li>
<li><p>When the user clicks on “Save”, the system will prompt a
confirmation message (Refer to MSG 1). If the user chooses Cancel, the
system does nothing; else, the system will save inputted information and
update the item.</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(4.3)</em></td>
<td style="text-align: center;"><em>BR55</em></td>
<td style="text-align: center;"><p><strong>Select payment method
Rules:</strong></p>
<ul>
<li><p>The user selects the following available payment
methods:</p></li>
</ul>
<ul>
<li><p>Cash on delivery (COD)</p></li>
<li><p>Payment via visa/mastercard</p></li>
<li><p>Payment via Momo e-wallet</p></li>
<li><p>Payment via PayPoo</p></li>
<li><p>Payment via ZaloPay</p></li>
<li><p>Payment via VNPAY</p></li>
<li><p>Payment via Paypal payment gateway</p></li>
<li><p>Payment via internet banking . A few affiliate banks are
available like: ABBank, ACB,AgriBank, Bac A Bank, Bảo Việt Bank, BIDV,
Đông á bank, Eximbank, GPBank, HDBank, Liên Việt Bank, MB Bank, Nam Á
Bank, NCB, OCB, OCEAN BANK, Sacombank, SCB, SeaBank, SHB, TechcomBank,
TPBank, VIB, VietABank, Vietcombank, Vietinbank, VPBank</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(6)</em></td>
<td style="text-align: center;"><em>BR56</em></td>
<td style="text-align: center;"><p><strong>Check payment method
rules:</strong></p>
<ul>
<li><p>The system checks the payment method the user has
chosen.</p></li>
</ul>
<ul>
<li><p>If it is an online payment, the user needs to complete the
payment according to the regulations states in
<strong>BR8</strong></p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(8)</em></td>
<td style="text-align: center;"><em>BR57</em></td>
<td style="text-align: center;"><p><strong>Make payment
rules</strong></p>
<ul>
<li><p>Switch ([paymentMethod])</p></li>
</ul>
<ul>
<li><p>case (‘visa/mastercard’) : the following information must be
filled in: [card number], [cardholder name], [card opening date] and
[CVV number]</p></li>
<li><p>case (‘Paypal’) : they need to enter their account information
correctly</p></li>
<li><p>case (‘Momo e-wallet’ || ‘ZaloPay’ || ‘VNPAY’ || ‘PayPoo’ ): they
need to do:</p></li>
</ul>
<p>- For TechMarket web: Scan the QR code with the Momo/Zalo/VNPAY
application on their phone to pay</p>
<p>- For TechMarket application on phone: Log in to Momo/Zalo/VNPAY on
phone to make payment. The system must navigate to the Momo application
and manually create an invoice with the following information: [Order
code], [Order value]</p>
<ul>
<li><p>case (‘internet banking’): they need to enter complete
information as required by each bank.</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(9.1)</em></td>
<td style="text-align: center;"><em>BR58</em></td>
<td style="text-align: center;"><p><strong>Message rules:</strong></p>
<ul>
<li><p>The system shows message MSG 4</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(9.2)</em></td>
<td style="text-align: center;"><em>BR59</em></td>
<td style="text-align: center;"><p><strong>Message rules:</strong></p>
<ul>
<li><p>The system shows message MSG 5</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(10)</em></td>
<td style="text-align: center;"><em>BR60</em></td>
<td style="text-align: center;"><p><strong>Saving rules:</strong></p>
<ul>
<li><p>In online payment case, the payment will be transferred to
TechMarket's account for safekeeping</p></li>
</ul>
<ul>
<li><p>The system creates an order as the Order
<strong>template</strong> below.</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
<td style="text-align: center;"><p><strong>Order template</strong></p>
<ul>
<li><p>[orderId] = &lt;&lt;automatic generated&gt;&gt;</p></li>
<li><p>[orderPrice] = &lt;&lt;total of price of products, shipping
fee&gt;&gt;</p></li>
<li><p>[userId] = &lt;&lt;current user’s id&gt;&gt;</p></li>
<li><p>[deliveryAddress] = &lt;&lt;address that user
entered&gt;&gt;</p></li>
<li><p>[paymentMethod] = &lt;&lt;method that user
chosen&gt;&gt;</p></li>
<li><p>if ([paymentMethod] = ‘COD’) then</p></li>
</ul>
<p>[paymentStatus] = ‘unpaid’</p>
<p>else [paymentStatus] = ‘paid’</p>
<ul>
<li><p>[shipmentStatus] = ‘order successful’</p></li>
<li><p>[orderStatus] = ‘wait for confirmation’</p></li>
<li><p>[productList[]] = &lt;&lt;list of product&gt;&gt;</p></li>
</ul></td>
</tr>
</tbody>
</table>

### UC13: Choose shipping address

<table>
<colgroup>
<col style="width: 21%" />
<col style="width: 78%" />
</colgroup>
<tbody>
<tr>
<td><strong>Name</strong></td>
<td><strong>Choose shipping address</strong></td>
</tr>
<tr>
<td><strong>Description</strong></td>
<td>This use case describes how a user can select an existing address or
create a new one.</td>
</tr>
<tr>
<td><strong>Actor</strong></td>
<td>Buyer</td>
</tr>
<tr>
<td><strong>Trigger</strong></td>
<td><ul>
<li><p>When the Buyer clicks on the “Choose address” button.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Pre-condition</strong></td>
<td><ul>
<li><p>The Buyer is logged in to the system.</p></li>
<li><p>The Buyer is on the place order page (refer to “Checkout” in the
“List description” file).</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Post-condition</strong></td>
<td><ul>
<li><p>The new address is saved.</p></li>
</ul></td>
</tr>
</tbody>
</table>

#### 

#### 

#### Activities Flow

<img src="media/image13.png" style="width:3.68906in;height:4.91874in" />

#### Business Rules

<table>
<colgroup>
<col style="width: 14%" />
<col style="width: 14%" />
<col style="width: 70%" />
</colgroup>
<tbody>
<tr>
<td style="text-align: center;"><strong>Activity</strong></td>
<td style="text-align: center;"><strong>BR Code</strong></td>
<td style="text-align: center;"><strong>Description</strong></td>
</tr>
<tr>
<td style="text-align: center;"><em>(2)</em></td>
<td style="text-align: center;"><em>BR61</em></td>
<td style="text-align: center;"><p><strong>Loading Dialog
Rules:</strong></p>
<ul>
<li><p>The system shows the address dialog</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(4)</em></td>
<td style="text-align: center;"><em>BR62</em></td>
<td style="text-align: center;"><p><strong>Loading Dialog
Rules:</strong></p>
<ul>
<li><p>The system shows create address dialog</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(5)</em></td>
<td style="text-align: center;"><em>BR63</em></td>
<td style="text-align: center;"><p><strong>Fill In Address
Rules:</strong></p>
<ul>
<li><p>The system displays the steps to fill in the address:</p></li>
</ul>
<p>[province_city] -&gt; [district] -&gt; [ward] -&gt;
[details]</p></td>
</tr>
<tr>
<td style="text-align: center;"><em>(7)</em></td>
<td style="text-align: center;"><em>BR64</em></td>
<td style="text-align: center;"><p><strong>Creating Rules:</strong></p>
<p>When the user clicks the “Create” button, the system will prompt a
confirmation message (Refer to MSG 1). If user chooses Cancel, the
system does nothing; else, the system will save inputted information and
update the item as the following:</p>
<ul>
<li><p>The system checks the items [province_city], [district], [ward],
[details].</p></li>
</ul>
<ul>
<li><p>If any entries are empty, the system shows an error message MSG
2.</p></li>
</ul>
<ul>
<li><p>[address.user] = &lt;&lt;current user id retrieved from
jwt&gt;&gt;</p></li>
<li><p>[address.createAt] = &lt;&lt;current date time&gt;&gt;</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(10)</em></td>
<td style="text-align: center;"><em>BR65</em></td>
<td style="text-align: center;"><p><strong>Saving Rules:</strong></p>
<ul>
<li><p>The currently created order is updated with:</p></li>
</ul>
<ul>
<li><p>[order.address] = &lt;&lt;chosen address&gt;&gt;</p></li>
</ul></td>
</tr>
</tbody>
</table>

### UC14: Pay

<table>
<colgroup>
<col style="width: 21%" />
<col style="width: 78%" />
</colgroup>
<tbody>
<tr>
<td><strong>Name</strong></td>
<td><strong>Pay</strong></td>
</tr>
<tr>
<td><strong>Description</strong></td>
<td>This use case describes how users can pay for orders through the
payment gateway.</td>
</tr>
<tr>
<td><strong>Actor</strong></td>
<td>Buyer</td>
</tr>
<tr>
<td><strong>Trigger</strong></td>
<td><ul>
<li><p>When the user clicks on the “Pay now” button .</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Pre-condition</strong></td>
<td><ul>
<li><p>The user is logged in to the system.</p></li>
<li><p>The user is on the checkout page (refer to “Checkout” in the
“List description” file).</p></li>
<li><p>The user has chosen Pay via payment gateway.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Post-condition</strong></td>
<td><ul>
<li><p>The payment has been created for order.</p></li>
<li><p>Money has been transferred to the system.</p></li>
</ul></td>
</tr>
</tbody>
</table>

#### 

#### 

#### Activities Flow

<img src="media/image14.png" style="width:6.69272in;height:3.98611in" />

#### Business Rules

<table>
<colgroup>
<col style="width: 14%" />
<col style="width: 14%" />
<col style="width: 70%" />
</colgroup>
<tbody>
<tr>
<td style="text-align: center;"><strong>Activity</strong></td>
<td style="text-align: center;"><strong>BR Code</strong></td>
<td style="text-align: center;"><strong>Description</strong></td>
</tr>
<tr>
<td style="text-align: center;"><em>(4)</em></td>
<td style="text-align: center;"><em>BR66</em></td>
<td style="text-align: center;"><p><strong>Creating request
rules:</strong></p>
<ul>
<li><p>The system extracts order information from the request.</p></li>
<li><p>[order] = request.body.order</p></li>
<li><p>Create [paymentRequest]</p></li>
<li><p>[paymentRequest.data] = [oder] to xml</p></li>
<li><p>[paymentRequest.checksum] = SHA512([paymentRequest.data] +
[private-key])</p></li>
<li><p>[paymentRequest.method] = request.body.method</p></li>
<li><p>paymentClient.send([paymentRequest])</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(5)</em></td>
<td style="text-align: center;"><em>BR67</em></td>
<td style="text-align: center;"><p><strong>Validate Rules:</strong></p>
<ul>
<li><p>The system extracts items from [paymentRequest]: [data],
[checksum] and [method]</p></li>
</ul>
<ul>
<li><p>If any entries are empty, the system shows an error message MSG
2.</p></li>
<li><p>If [checksum] is not valid or [method] is unsupported then the
system returns an error response.</p></li>
</ul>
<blockquote>
<p>Else the system saves order information and creates a link for paying
this order.</p>
</blockquote></td>
</tr>
<tr>
<td style="text-align: center;"><em>(9)</em></td>
<td style="text-align: center;"><em>BR68</em></td>
<td style="text-align: center;"><p><strong>Validate Rules:</strong></p>
<ul>
<li><p>The system extracts the response from payment service</p></li>
<li><p>If [response.status] == 200 then</p></li>
</ul>
<blockquote>
<p>[order] = Order repository find by id [response.order.id]</p>
<p>[payment] = Payment repository creates new payment.</p>
<p>[payment.order] = [order]</p>
<p>[payment.createdDate] = Date.now()</p>
<p>[payment.provider] = corresponding provider</p>
<p>[payment.method] = [response.method]</p>
<p>[payment.status] = ‘PENDING’</p>
<p>return success response with [payment] and payment gateway link
[response.link]</p>
</blockquote>
<p>else the system returns an error message MSG37.</p></td>
</tr>
<tr>
<td style="text-align: center;"><em>(11)</em></td>
<td style="text-align: center;"><em>BR69</em></td>
<td style="text-align: center;"><p><strong>Redirect Rules:</strong></p>
<ul>
<li><p>The system redirects the user to the link that has been received
from the payment service.</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(12)</em></td>
<td style="text-align: center;"><em>BR70</em></td>
<td style="text-align: center;"><p><strong>Payment processing
rules:</strong></p>
<ul>
<li><p>The system updates the corresponding payment.</p></li>
</ul>
<ul>
<li><p>[payment.status] = ‘PROCESSING’</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(14)</em></td>
<td style="text-align: center;"><em>BR71</em></td>
<td style="text-align: center;"><p><strong>Update payment success
rules</strong></p>
<ul>
<li><p>The system updates the corresponding payment.</p></li>
</ul>
<ul>
<li><p>[payment.status] = ‘COMPLETED’</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(15)</em></td>
<td style="text-align: center;"><em>BR72</em></td>
<td style="text-align: center;"><p><strong>Sending email
rules:</strong></p>
<ul>
<li><p><strong>Email Templates:</strong></p></li>
<li><p>Send mail to user after successful order
payment:<strong>:</strong></p></li>
</ul>
<table>
<colgroup>
<col style="width: 25%" />
<col style="width: 74%" />
</colgroup>
<tbody>
<tr>
<td><blockquote>
<p>From</p>
</blockquote></td>
<td><blockquote>
<p>techmarket@gmail.com</p>
</blockquote></td>
</tr>
<tr>
<td><blockquote>
<p>To</p>
</blockquote></td>
<td><blockquote>
<p>[email]</p>
</blockquote></td>
</tr>
<tr>
<td><blockquote>
<p>Cc</p>
</blockquote></td>
<td><blockquote>
<p>N/A</p>
</blockquote></td>
</tr>
<tr>
<td><blockquote>
<p>Subject</p>
</blockquote></td>
<td><blockquote>
<p>Get [Subject] of “Email Template” item of which [Keyword] = “Order
Payment Success”</p>
</blockquote></td>
</tr>
<tr>
<td><blockquote>
<p>Body</p>
</blockquote></td>
<td><blockquote>
<p>Get [Body] of “Email Template” item of which [Keyword] = “Order
Payment Success”</p>
</blockquote></td>
</tr>
</tbody>
</table>
<ul>
<li><p>Following is a sample email content:</p></li>
</ul>
<table>
<colgroup>
<col style="width: 25%" />
<col style="width: 74%" />
</colgroup>
<tbody>
<tr>
<td><blockquote>
<p>Subject</p>
</blockquote></td>
<td><blockquote>
<p>"Order Payment Confirmation - Tech Market"</p>
</blockquote></td>
</tr>
<tr>
<td><blockquote>
<p>Body</p>
</blockquote></td>
<td><blockquote>
<p>[Body] = “Hello,”</p>
<p>[Body] = [Body] + 2 new lines</p>
<p>[Body] = [Body] + “Thank you for your purchase! Your order payment
has been successfully processed.”</p>
<p>[Body] = [Body] + 2 new lines</p>
<p>[Body] = [Body] + “- Order Number: [Order Number]”</p>
<p>[Body] = [Body] + “- Payment Amount: [Payment Amount]”</p>
<p>[Body] = [Body] + “- Payment Method: [Payment Method]”</p>
<p>[Body] = [Body] + “Your items will be shipped shortly. You can track
your order using the provided tracking details.”</p>
<p>[Body] = [Body] + "<mark>If you have any questions or concerns,
please feel free to contact our customer support.</mark>"</p>
<p>[Body] = [Body] + 2 new lines</p>
<p>[Body] = [Body] + "<mark>Thanks for shopping with Tech
Market!</mark>"</p>
<p>[Body] = [Body] + 2 new lines</p>
<p>[Body] = [Body] + "<mark>Best regards,</mark>"</p>
<p>[Body] = [Body] + 2 new lines</p>
<p>[Body] = [Body] + "The <mark>Tech Market team</mark>"</p>
</blockquote></td>
</tr>
</tbody>
</table></td>
</tr>
<tr>
<td style="text-align: center;"><em>(16)</em></td>
<td style="text-align: center;"><em>BR73</em></td>
<td style="text-align: center;"><p><strong>Message rules:</strong></p>
<ul>
<li><p>The system shows message MSG 38</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(17)</em></td>
<td style="text-align: center;"><em>BR74</em></td>
<td style="text-align: center;"><p><strong>Message rules:</strong></p>
<ul>
<li><p>The system shows message MSG 39</p></li>
</ul></td>
</tr>
</tbody>
</table>

### UC15: Cancel order

<table>
<colgroup>
<col style="width: 21%" />
<col style="width: 78%" />
</colgroup>
<tbody>
<tr>
<td><strong>Name</strong></td>
<td><strong>Cancel order</strong></td>
</tr>
<tr>
<td><strong>Description</strong></td>
<td>This use case describes how a user can cancel an order</td>
</tr>
<tr>
<td><strong>Actor</strong></td>
<td>Buyer</td>
</tr>
<tr>
<td><strong>Trigger</strong></td>
<td><ul>
<li><p>When the Buyer clicks the cancel button.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Pre-condition</strong></td>
<td><ul>
<li><p>The Buyer is logged in to the system.</p></li>
<li><p>The user is on the order details page (refer to “Buyer/Order
Details” in the “List description” file).</p></li>
<li><p>The order has been created and is in CREATED status</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Post-condition</strong></td>
<td><ul>
<li><p>The order was cancelled successfully.</p></li>
</ul></td>
</tr>
</tbody>
</table>

#### 

#### 

#### Activities Flow

<img src="media/image15.png" style="width:5.41667in;height:3.96875in" />

#### Business Rules

<table>
<colgroup>
<col style="width: 14%" />
<col style="width: 14%" />
<col style="width: 70%" />
</colgroup>
<tbody>
<tr>
<td style="text-align: center;"><strong>Activity</strong></td>
<td style="text-align: center;"><strong>BR Code</strong></td>
<td style="text-align: center;"><strong>Description</strong></td>
</tr>
<tr>
<td style="text-align: center;"><em>(3)</em></td>
<td style="text-align: center;"><em>BR75</em></td>
<td style="text-align: center;"><p><strong>Message Rules:</strong></p>
<ul>
<li><p>The system will prompt a confirmation message (Refer to MSG 1).
If user chooses Cancel, the system does nothing;</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(4)</em></td>
<td style="text-align: center;"><em>BR76</em></td>
<td style="text-align: center;"><p><strong>Check payment information
Rules:</strong></p>
<ul>
<li><p>The system checks order payment information and is ready to
refund within 7 days.</p></li>
<li><p>When the refund is successful, [order.payment_status] =
REFUND</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(5)</em></td>
<td style="text-align: center;"><em>BR77</em></td>
<td style="text-align: center;"><p><strong>Cancelling
Rules:</strong></p>
<ul>
<li><p>[order.status] = CANCEL</p></li>
</ul></td>
</tr>
</tbody>
</table>

### 

### UC16: Confirm delivery of order

<table>
<colgroup>
<col style="width: 21%" />
<col style="width: 78%" />
</colgroup>
<tbody>
<tr>
<td><strong>Name</strong></td>
<td><strong>Confirm delivery of order</strong></td>
</tr>
<tr>
<td><strong>Description</strong></td>
<td>This use case allows Buyer to confirm delivery of order.</td>
</tr>
<tr>
<td><strong>Actor</strong></td>
<td>Buyer</td>
</tr>
<tr>
<td><strong>Trigger</strong></td>
<td><ul>
<li><p>When the user clicks on the “Received” button.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Pre-condition</strong></td>
<td><ul>
<li><p>The user is logged in to the system.</p></li>
<li><p>The user is on the order details page (refer to “Buyer/Order
Details” in the “List description” file).</p></li>
<li><p>The order has been successfully delivered by the shipping
unit.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Post-condition</strong></td>
<td><ul>
<li><p>The money is transferred to the seller's account.</p></li>
</ul></td>
</tr>
</tbody>
</table>

#### Activities Flow

<img src="media/image16.png" style="width:6.26718in;height:4.35884in" />

#### Business Rules

<table>
<colgroup>
<col style="width: 14%" />
<col style="width: 12%" />
<col style="width: 73%" />
</colgroup>
<tbody>
<tr>
<td style="text-align: center;"><strong>Activity</strong></td>
<td style="text-align: center;"><strong>BR Code</strong></td>
<td style="text-align: center;"><strong>Description</strong></td>
</tr>
<tr>
<td style="text-align: center;"><em>(2)</em></td>
<td style="text-align: center;"><em>BR78</em></td>
<td style="text-align: center;"><p><strong>Message rules:</strong></p>
<ul>
<li><p>The system shows message MSG 6</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(4)</em></td>
<td style="text-align: center;"><em>BR79</em></td>
<td style="text-align: center;"><p><strong>Money transfer
rules:</strong></p>
<ul>
<li><p>Upon successful delivery, payment funds previously kept securely
in TechMarket's account will be transferred to the seller's
account.</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(6)</em></td>
<td style="text-align: center;"><em>BR80</em></td>
<td style="text-align: center;"><p><strong>Notification
rules:</strong></p>
<ul>
<li><p>The system sends a notification to the seller's dialog box
according to the following template:</p></li>
<li><p>[Buyer name] + ' has successfully confirmed receipt of the order
with code ' + [Order code].</p></li>
</ul></td>
</tr>
</tbody>
</table>

### UC17: Rate seller

<table>
<colgroup>
<col style="width: 21%" />
<col style="width: 78%" />
</colgroup>
<tbody>
<tr>
<td><strong>Name</strong></td>
<td><strong>Rate seller</strong></td>
</tr>
<tr>
<td><strong>Description</strong></td>
<td>This use case describes how buyer can rate the seller after
receiving order</td>
</tr>
<tr>
<td><strong>Actor</strong></td>
<td>Buyer</td>
</tr>
<tr>
<td><strong>Trigger</strong></td>
<td><ul>
<li><p>When the user clicks on the “Rate” button.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Pre-condition</strong></td>
<td><ul>
<li><p>The user is logged in to the system.</p></li>
<li><p>The order has been successfully confirmed to be shipped by the
shipping unit.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Post-condition</strong></td>
<td><ul>
<li><p>Seller’s rating has been saved.</p></li>
</ul></td>
</tr>
</tbody>
</table>

#### Activities Flow

<img src="media/image17.png" style="width:6.69272in;height:4.61111in" />

#### Business Rules

<table>
<colgroup>
<col style="width: 14%" />
<col style="width: 12%" />
<col style="width: 73%" />
</colgroup>
<tbody>
<tr>
<td style="text-align: center;"><strong>Activity</strong></td>
<td style="text-align: center;"><strong>BR Code</strong></td>
<td style="text-align: center;"><strong>Description</strong></td>
</tr>
<tr>
<td style="text-align: center;"><em>(2)</em></td>
<td style="text-align: center;"><em>BR81</em></td>
<td style="text-align: center;"><p><strong>Loading screen
rules:</strong></p>
<ul>
<li><p>The system loads the Rate page (refer to “Rate Seller Form” in
the “List description” file).</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(4)</em></td>
<td style="text-align: center;"><em>BR82</em></td>
<td style="text-align: center;"><p><strong>Validate rules:</strong></p>
<ul>
<li><p>The system extracts the rating data from request: [buyerId],
[sellerId], [postId], [comment] and [rating]</p></li>
<li><p>If any of them is null or empty then the system returns error
message MSG 2.</p></li>
<li><p>[buyer] = User repository find by id [buyerId] (call
userRepository.findById() function)</p></li>
<li><p>[seller] = User repository find by id [sellerId] (call
userRepository.findById() function)</p></li>
<li><p>[post] = Post repository find by id [postId] (call
postRepository.findById() function)</p></li>
<li><p>If [buyer] == null then the system returns error response with
status code 400 BAD_REQUEST</p></li>
<li><p>If [seller] == null then the system returns error response with
status code 400 BAD_REQUEST</p></li>
<li><p>If [post] == null then the system returns error response with
status code 400 BAD_REQUEST</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(5)</em></td>
<td style="text-align: center;"><em>BR83</em></td>
<td style="text-align: center;"><p><strong>Rate rules:</strong></p>
<ul>
<li><p>[rate] = Rating repository create new.</p></li>
<li><p>[rate.buyer] = [buyer]</p></li>
<li><p>[rate.seller] = [seller]</p></li>
<li><p>[rate.buyer] = [buyer]</p></li>
<li><p>[rate.rating] = [rating]</p></li>
<li><p>[rate.comment] = [comment]</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(6)</em></td>
<td style="text-align: center;"><em>BR84</em></td>
<td style="text-align: center;"><p><strong>Message rules:</strong></p>
<ul>
<li><p>The system shows message MSG 42.</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(7)</em></td>
<td style="text-align: center;"><em>BR85</em></td>
<td style="text-align: center;"><p><strong>Message rules:</strong></p>
<ul>
<li><p>The system shows message MSG 41.</p></li>
</ul></td>
</tr>
</tbody>
</table>

### 

### UC18: Chat with seller

<table>
<colgroup>
<col style="width: 21%" />
<col style="width: 78%" />
</colgroup>
<tbody>
<tr>
<td><strong>Name</strong></td>
<td><strong>Chat with seller</strong></td>
</tr>
<tr>
<td><strong>Description</strong></td>
<td>This use case describes how users can view post and product
details.</td>
</tr>
<tr>
<td><strong>Actor</strong></td>
<td>User</td>
</tr>
<tr>
<td><strong>Trigger</strong></td>
<td><ul>
<li><p>When the user clicks on the “Chat with seller” button.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Pre-condition</strong></td>
<td><ul>
<li><p>The user is on the post details page (refer to “User/Post
Details” in the “List description” file).</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Post-condition</strong></td>
<td><ul>
<li><p>The user is redirected to the Chat page.</p></li>
</ul></td>
</tr>
</tbody>
</table>

#### Activities Flow

<img src="media/image18.png" style="width:6.69272in;height:3.86111in" />

#### Business Rules

<table>
<colgroup>
<col style="width: 14%" />
<col style="width: 12%" />
<col style="width: 73%" />
</colgroup>
<tbody>
<tr>
<td style="text-align: center;"><strong>Activity</strong></td>
<td style="text-align: center;"><strong>BR Code</strong></td>
<td style="text-align: center;"><strong>Description</strong></td>
</tr>
<tr>
<td style="text-align: center;"><em>(2)</em></td>
<td style="text-align: center;"><em>BR86</em></td>
<td style="text-align: center;"><p><strong>Redirect Rules:</strong></p>
<ul>
<li><p>The system loads the Chat page (refer to “Chat” in the “List
description” file).</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(3)</em></td>
<td style="text-align: center;"><em>BR87</em></td>
<td style="text-align: center;"><p><strong>Sending Rules:</strong></p>
<ul>
<li><p>The system extracts the [sellerId] that the user wants to chat
with and [userId] from the request.</p></li>
<li><p>If the socket connection with key [sellerId]-[userId] not exists
then [channel] = socket.open()</p></li>
<li><p>The systems extracts [message] from request</p></li>
<li><p>If the [message] contains impolite words then the system shows
error message MSG 35.</p></li>
</ul>
<blockquote>
<p>else [channel].push([message])</p>
</blockquote></td>
</tr>
</tbody>
</table>

### UC19: Report post

<table>
<colgroup>
<col style="width: 21%" />
<col style="width: 78%" />
</colgroup>
<tbody>
<tr>
<td><strong>Name</strong></td>
<td><strong>Report post</strong></td>
</tr>
<tr>
<td><strong>Description</strong></td>
<td>This use case describes how users can report violative posts</td>
</tr>
<tr>
<td><strong>Actor</strong></td>
<td>Buyer</td>
</tr>
<tr>
<td><strong>Trigger</strong></td>
<td><ul>
<li><p>When the Buyer clicks the ‘Report’ button.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Pre-condition</strong></td>
<td><ul>
<li><p>The Buyer is logged in to the system.</p></li>
<li><p>The user is on the post details page (refer to “User/Post
Details” in the “List description” file).</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Post-condition</strong></td>
<td><ul>
<li><p>The report is created and sent to Admin to review.</p></li>
</ul></td>
</tr>
</tbody>
</table>

#### Activities Flow

<img src="media/image19.png" style="width:5.15625in;height:5.65625in" />

#### Business Rules

<table>
<colgroup>
<col style="width: 14%" />
<col style="width: 12%" />
<col style="width: 73%" />
</colgroup>
<tbody>
<tr>
<td style="text-align: center;"><strong>Activity</strong></td>
<td style="text-align: center;"><strong>BR Code</strong></td>
<td style="text-align: center;"><strong>Description</strong></td>
</tr>
<tr>
<td style="text-align: center;"><em>(3)</em></td>
<td style="text-align: center;"><em>BR88</em></td>
<td style="text-align: center;"><p><strong>Loading Dialog
Rules:</strong></p>
<ul>
<li><p>The system loads the “Report list” dialog.</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(4)</em></td>
<td style="text-align: center;"><em>BR89</em></td>
<td style="text-align: center;"><p><strong>Report Reason List
Rules:</strong></p>
<ul>
<li><p>The system shows the following options:</p></li>
</ul>
<ul>
<li><p>Cheat</p></li>
<li><p>Duplicate</p></li>
<li><p>Goods sold</p></li>
<li><p>Unable to contact</p></li>
<li><p>Information is not factually correct</p></li>
<li><p>Counterfeit goods, fake goods</p></li>
<li><p>Goods damaged after purchase</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(5)</em></td>
<td style="text-align: center;"><em>BR90</em></td>
<td style="text-align: center;"><p><strong>Fill in contact information
rules:</strong></p>
<ul>
<li><p>The system request user to provide the following
information:</p></li>
</ul>
<ul>
<li><p>[report.phone_number]</p></li>
<li><p>[report.email]</p></li>
<li><p>[report.description]</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(7)</em></td>
<td style="text-align: center;"><em>BR91</em></td>
<td style="text-align: center;"><p><strong>Creating Rules:</strong></p>
<p>When the user clicks the “Report” button, the system will prompt a
confirmation message (Refer to MSG 1). If user chooses Cancel, the
system does nothing; else, the system will save inputted information and
update the item as the following:</p>
<ul>
<li><p>The system check the following items : [report.phone_number],
[report.email], [report.reason], [report.description]</p></li>
</ul>
<ul>
<li><p>If any entries are empty, the system shows an error message MSG
2.</p></li>
<li><p>If [report.phone_number] is invalid then the system shows message
MSG 30</p></li>
<li><p>If [report.email] is invalid then the system shows message MSG
31</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(8)</em></td>
<td style="text-align: center;"><em>BR92</em></td>
<td style="text-align: center;"><p><strong>Message Rules:</strong></p>
<ul>
<li><p>The system shows message MSG 47</p></li>
</ul></td>
</tr>
</tbody>
</table>

### 

### UC20: Create category

<table>
<colgroup>
<col style="width: 21%" />
<col style="width: 78%" />
</colgroup>
<tbody>
<tr>
<td><strong>Name</strong></td>
<td><strong>Create category</strong></td>
</tr>
<tr>
<td><strong>Description</strong></td>
<td>This use case describes how users can create new categories for the
system</td>
</tr>
<tr>
<td><strong>Actor</strong></td>
<td>Admin</td>
</tr>
<tr>
<td><strong>Trigger</strong></td>
<td><ul>
<li><p>When the Admin clicks the “Create” button.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Pre-condition</strong></td>
<td><ul>
<li><p>The Admin is logged in to the system.</p></li>
<li><p>The admin accessed the category screen.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Post-condition</strong></td>
<td><ul>
<li><p>The new category is created.</p></li>
</ul></td>
</tr>
</tbody>
</table>

#### Activities Flow

<img src="media/image20.png" style="width:6.69272in;height:5in" />

#### Business Rules

<table>
<colgroup>
<col style="width: 14%" />
<col style="width: 12%" />
<col style="width: 73%" />
</colgroup>
<tbody>
<tr>
<td style="text-align: center;"><strong>Activity</strong></td>
<td style="text-align: center;"><strong>BR Code</strong></td>
<td style="text-align: center;"><strong>Description</strong></td>
</tr>
<tr>
<td style="text-align: center;"><em>(2)</em></td>
<td style="text-align: center;"><em>BR93</em></td>
<td style="text-align: center;"><p><strong>Loading Screen
Rules:</strong></p>
<ul>
<li><p>The system loads the “Create category” screen (refer to “Create
Category” in the “List description” file).</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(3)</em></td>
<td style="text-align: center;"><em>BR94</em></td>
<td style="text-align: center;"><p><strong>Fill In Category
Rules:</strong></p>
<ul>
<li><p>The system request user to fill in the following
information:</p></li>
</ul>
<ul>
<li><p>[category.name]</p></li>
<li><p>[category.description]</p></li>
<li><p>[category.brands] = [ {</p></li>
</ul>
<blockquote>
<p>‘name’:</p>
<p>‘logo’:</p>
<p>} ]</p>
</blockquote></td>
</tr>
<tr>
<td style="text-align: center;"><em>(5)</em></td>
<td style="text-align: center;"><em>BR95</em></td>
<td style="text-align: center;"><p><strong>Checking Category
Rules:</strong></p>
<p>When the user clicks the “Create” button, the system will prompt a
confirmation message (Refer to MSG 1). If user chooses Cancel, the
system does nothing; else, the system will save inputted information and
update the item as the following:</p>
<ul>
<li><p>The system check the following items : [category.name],
[category.description],[category.brands]</p></li>
</ul>
<ul>
<li><p>If any entries are empty, the system shows an error message MSG
2.</p></li>
<li><p>If [category.name] is already exist then the system return
response with status code 400</p></li>
</ul>
<blockquote>
<p>else the system return response with status code 200</p>
</blockquote></td>
</tr>
<tr>
<td style="text-align: center;"><em>(5.1)</em></td>
<td style="text-align: center;"><em>BR96</em></td>
<td style="text-align: center;"><p><strong>Saving Rules:</strong></p>
<ul>
<li><p>The system save the new category with the following
entries:</p></li>
<li><p>[category.name]</p></li>
<li><p>[category.description]</p></li>
<li><p>[category.brands] = [{</p></li>
</ul>
<blockquote>
<p>‘name’:</p>
<p>‘logo’:</p>
<p>} ]</p>
</blockquote></td>
</tr>
<tr>
<td style="text-align: center;"><em>(5.2)</em></td>
<td style="text-align: center;"><em>BR97</em></td>
<td style="text-align: center;"><p><strong>Message Rules:</strong></p>
<ul>
<li><p>The system shows message MSG 49</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(6)</em></td>
<td style="text-align: center;"><em>BR98</em></td>
<td style="text-align: center;"><p><strong>Message Rules:</strong></p>
<ul>
<li><p>The system shows message MSG 48</p></li>
</ul></td>
</tr>
</tbody>
</table>

### 

### UC21: Update category 

<table>
<colgroup>
<col style="width: 21%" />
<col style="width: 78%" />
</colgroup>
<tbody>
<tr>
<td><strong>Name</strong></td>
<td><strong>Update category</strong></td>
</tr>
<tr>
<td><strong>Description</strong></td>
<td>This use case describes how a user can change the category
information</td>
</tr>
<tr>
<td><strong>Actor</strong></td>
<td>Admin</td>
</tr>
<tr>
<td><strong>Trigger</strong></td>
<td><ul>
<li><p>When the Admin clicks the “Edit” button.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Pre-condition</strong></td>
<td><ul>
<li><p>The Admin is logged in to the system.</p></li>
<li><p>The admin accessed the category screen.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Post-condition</strong></td>
<td><ul>
<li><p>The category is updated.</p></li>
</ul></td>
</tr>
</tbody>
</table>

#### Activities Flow

<img src="media/image21.png" style="width:6.24114in;height:5.96594in" />

#### Business Rules

<table>
<colgroup>
<col style="width: 14%" />
<col style="width: 12%" />
<col style="width: 73%" />
</colgroup>
<tbody>
<tr>
<td style="text-align: center;"><strong>Activity</strong></td>
<td style="text-align: center;"><strong>BR Code</strong></td>
<td style="text-align: center;"><strong>Description</strong></td>
</tr>
<tr>
<td style="text-align: center;"><em>(2)</em></td>
<td style="text-align: center;"><em>BR99</em></td>
<td style="text-align: center;"><p><strong>Loading Screen
Rules:</strong></p>
<ul>
<li><p>The system loads the “Category details” screen.</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(4)</em></td>
<td style="text-align: center;"><em>BR100</em></td>
<td style="text-align: center;"><p><strong>Loading Screen
Rules:</strong></p>
<ul>
<li><p>The system loads the “Category edit” screen.</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(7)</em></td>
<td style="text-align: center;"><em>BR101</em></td>
<td style="text-align: center;"><p><strong>Checking Category
Rules:</strong></p>
<p>When the user clicks the “Save” button, the system will prompt a
confirmation message (Refer to MSG 1). If user chooses Cancel, the
system does nothing; else, the system will save inputted information and
update the item as the following:</p>
<ul>
<li><p>The system check the following items : [category.name],
[category.description],[category.brands]</p></li>
</ul>
<ul>
<li><p>If any entries are empty, the system shows an error message MSG
2.</p></li>
<li><p>If [category.name] is already exist then the system return
response with status code 400</p></li>
</ul>
<blockquote>
<p>else the system return response with status code 200</p>
</blockquote></td>
</tr>
<tr>
<td style="text-align: center;"><em>(7.1)</em></td>
<td style="text-align: center;"><em>BR102</em></td>
<td style="text-align: center;"><p><strong>Saving Rules:</strong></p>
<ul>
<li><p>The system save the new category with the following
entries:</p></li>
<li><p>[category.name]</p></li>
<li><p>[category.description]</p></li>
<li><p>[category.brands] = [{</p></li>
</ul>
<blockquote>
<p>‘name’:</p>
<p>‘logo’:</p>
<p>} ]</p>
</blockquote></td>
</tr>
<tr>
<td style="text-align: center;"><em>(7.2)</em></td>
<td style="text-align: center;"><em>BR103</em></td>
<td style="text-align: center;"><p><strong>Message Rules:</strong></p>
<ul>
<li><p>The system shows message MSG 49</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(8)</em></td>
<td style="text-align: center;"><em>BR104</em></td>
<td style="text-align: center;"><p><strong>Message Rules:</strong></p>
<ul>
<li><p>The system shows message MSG 50</p></li>
</ul></td>
</tr>
</tbody>
</table>

### UC22: Delete category

<table>
<colgroup>
<col style="width: 21%" />
<col style="width: 78%" />
</colgroup>
<tbody>
<tr>
<td><strong>Name</strong></td>
<td><strong>Report post</strong></td>
</tr>
<tr>
<td><strong>Description</strong></td>
<td>This use case describes how a user can delete a category</td>
</tr>
<tr>
<td><strong>Actor</strong></td>
<td>Admin</td>
</tr>
<tr>
<td><strong>Trigger</strong></td>
<td><ul>
<li><p>When the Admin clicks the “Delete” button.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Pre-condition</strong></td>
<td><ul>
<li><p>The Admin is logged in to the system.</p></li>
<li><p>The Admin accessed the category screen.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Post-condition</strong></td>
<td><ul>
<li><p>The categories are deleted.</p></li>
</ul></td>
</tr>
</tbody>
</table>

#### Activities Flow

<img src="media/image22.png" style="width:6.69272in;height:4.18056in" />

#### Business Rules

<table>
<colgroup>
<col style="width: 14%" />
<col style="width: 12%" />
<col style="width: 73%" />
</colgroup>
<tbody>
<tr>
<td style="text-align: center;"><strong>Activity</strong></td>
<td style="text-align: center;"><strong>BR Code</strong></td>
<td style="text-align: center;"><strong>Description</strong></td>
</tr>
<tr>
<td style="text-align: center;"><em>(3)</em></td>
<td style="text-align: center;"><em>BR105</em></td>
<td style="text-align: center;"><p><strong>Loading Screen
Rules:</strong></p>
<ul>
<li><p>The system shows message MSG 51</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(5)</em></td>
<td style="text-align: center;"><em>BR106</em></td>
<td style="text-align: center;"><p><strong>Changing Rules:</strong></p>
<p>For each selected categories:</p>
<ul>
<li><p>[postRepository].findAllByCategory([selectedCategory])</p></li>
<li><p>for each post found: [post.category] =
[categoryRepository].findByName(‘other’)</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(6)</em></td>
<td style="text-align: center;"><em>BR107</em></td>
<td style="text-align: center;"><p><strong>Deleting Rules:</strong></p>
<p>For each selected categories:</p>
<ul>
<li><p>If ( [categoryRepository].existById([selectedCategory.id]) ==
true ) then [categoryRepository].deleteById([selectedCategory])</p></li>
</ul>
<p>else the system returns a response with status code 400 and shows
message MSG 49</p></td>
</tr>
</tbody>
</table>

### 

### 

### UC23: Approve post

<table>
<colgroup>
<col style="width: 21%" />
<col style="width: 78%" />
</colgroup>
<tbody>
<tr>
<td><strong>Name</strong></td>
<td><strong>Approve post</strong></td>
</tr>
<tr>
<td><strong>Description</strong></td>
<td>This use case allows the Administrator to approve the user's
post.</td>
</tr>
<tr>
<td><strong>Actor</strong></td>
<td>Administrator</td>
</tr>
<tr>
<td><strong>Trigger</strong></td>
<td><ul>
<li><p>When the Administrator clicks on the “Approve” button.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Pre-condition</strong></td>
<td><ul>
<li><p>The Administrator is logged in to the system with ‘Admin’
permission.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Post-condition</strong></td>
<td><ul>
<li><p>The post is approved and visible to all users.</p></li>
</ul></td>
</tr>
</tbody>
</table>

#### Activities Flow

<img src="media/image23.png" style="width:6.08333in;height:6.02083in" />

#### Business Rules

<table>
<colgroup>
<col style="width: 14%" />
<col style="width: 12%" />
<col style="width: 73%" />
</colgroup>
<tbody>
<tr>
<td style="text-align: center;"><strong>Activity</strong></td>
<td style="text-align: center;"><strong>BR Code</strong></td>
<td style="text-align: center;"><strong>Description</strong></td>
</tr>
<tr>
<td style="text-align: center;"><em>(2)</em></td>
<td style="text-align: center;"><em>BR108</em></td>
<td style="text-align: center;"><p><strong>Loading Screen
Rules:</strong></p>
<ul>
<li><p>The system loads the “Post details” screen (refer to “Admin/Post
Details” in the “List description” file).</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(4)</em></td>
<td style="text-align: center;"><em>BR109</em></td>
<td style="text-align: center;"><p><strong>Message Rules:</strong></p>
<ul>
<li><p>The system shows message MSG 7</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(5)</em></td>
<td style="text-align: center;"><em>BR110</em></td>
<td style="text-align: center;"><p><strong>Approving rules:</strong></p>
<ul>
<li><p>If the current user is not ADMIN then the system shows error
message MSG 15.</p></li>
<li><p>The system will update the item as the following:</p></li>
</ul>
<ul>
<li><p>[status] = APPROVED</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(6)</em></td>
<td style="text-align: center;"><em>BR111</em></td>
<td style="text-align: center;"><p><strong>Message Rules:</strong></p>
<ul>
<li><p>The system shows message MSG 8</p></li>
</ul></td>
</tr>
</tbody>
</table>

### 

### 

### 

### UC24: Reject post

<table>
<colgroup>
<col style="width: 21%" />
<col style="width: 78%" />
</colgroup>
<tbody>
<tr>
<td><strong>Name</strong></td>
<td><strong>Reject post</strong></td>
</tr>
<tr>
<td><strong>Description</strong></td>
<td>This use case allows the Administrator to reject the user's
post.</td>
</tr>
<tr>
<td><strong>Actor</strong></td>
<td>Administrator</td>
</tr>
<tr>
<td><strong>Trigger</strong></td>
<td><ul>
<li><p>When the Administrator clicks on the “Reject” button.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Pre-condition</strong></td>
<td><ul>
<li><p>The Administrator is logged in to the system with ‘Admin’
permission.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Post-condition</strong></td>
<td><ul>
<li><p>The post is rejected and deleted.</p></li>
</ul></td>
</tr>
</tbody>
</table>

#### Activities Flow

<img src="media/image24.png" style="width:4.44427in;height:4.25223in" />

*Figure 1: Activities Flow*

#### Business Rules

<table>
<colgroup>
<col style="width: 14%" />
<col style="width: 12%" />
<col style="width: 73%" />
</colgroup>
<tbody>
<tr>
<td style="text-align: center;"><strong>Activity</strong></td>
<td style="text-align: center;"><strong>BR Code</strong></td>
<td style="text-align: center;"><strong>Description</strong></td>
</tr>
<tr>
<td style="text-align: center;"><em>(2)</em></td>
<td style="text-align: center;"><em>BR112</em></td>
<td style="text-align: center;"><p><strong>Loading Screen
Rules:</strong></p>
<ul>
<li><p>The system loads the “Post details” screen (refer to “Admin/Post
Details” in the “List description” file).</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(4)</em></td>
<td style="text-align: center;"><em>BR113</em></td>
<td style="text-align: center;"><p><strong>Message Rules:</strong></p>
<ul>
<li><p>The system shows message MSG 9</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(5)</em></td>
<td style="text-align: center;"><em>BR114</em></td>
<td style="text-align: center;"><p><strong>Notification
Rules:</strong></p>
<ul>
<li><p>The system sends a notification to the seller's dialog box
according to the following template:</p></li>
<li><p>‘Your post with the title’+ [Name] + ‘has been approved by the
Administrator. \nNow everyone can see your posts.’</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(7)</em></td>
<td style="text-align: center;"><em>BR115</em></td>
<td style="text-align: center;"><p><strong>Message Rules:</strong></p>
<ul>
<li><p>The system shows message MSG 10</p></li>
</ul></td>
</tr>
</tbody>
</table>

### UC25: Ban account

<table>
<colgroup>
<col style="width: 21%" />
<col style="width: 78%" />
</colgroup>
<tbody>
<tr>
<td><strong>Name</strong></td>
<td><strong>Ban account</strong></td>
</tr>
<tr>
<td><strong>Description</strong></td>
<td>This use case describes how a user can ban a user account that
violates website’s regulations</td>
</tr>
<tr>
<td><strong>Actor</strong></td>
<td>Admin</td>
</tr>
<tr>
<td><strong>Trigger</strong></td>
<td><ul>
<li><p>When the Admin clicks the “Ban” button.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Pre-condition</strong></td>
<td><ul>
<li><p>The Admin is logged in to the system.</p></li>
<li><p>The Admin accessed the user screen.</p></li>
<li><p>The user account has been reported in violation.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Post-condition</strong></td>
<td><ul>
<li><p>The user account is banned.</p></li>
</ul></td>
</tr>
</tbody>
</table>

#### Activities Flow

<img src="media/image25.png" style="width:5.33229in;height:4.87619in" />

#### Business Rules

<table>
<colgroup>
<col style="width: 14%" />
<col style="width: 12%" />
<col style="width: 73%" />
</colgroup>
<tbody>
<tr>
<td style="text-align: center;"><strong>Activity</strong></td>
<td style="text-align: center;"><strong>BR Code</strong></td>
<td style="text-align: center;"><strong>Description</strong></td>
</tr>
<tr>
<td style="text-align: center;"><em>(2)</em></td>
<td style="text-align: center;"><em>BR116</em></td>
<td style="text-align: center;"><p><strong>Loading Screen
Rules:</strong></p>
<ul>
<li><p>The system loads the “Report details” screen</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(4)</em></td>
<td style="text-align: center;"><em>BR117</em></td>
<td style="text-align: center;"><p><strong>Changing Rules:</strong></p>
<ul>
<li><p>[user.status] = VIOLATE</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(5)</em></td>
<td style="text-align: center;"><em>BR118</em></td>
<td style="text-align: center;"><p><strong>Sending email
rules:</strong></p>
<ul>
<li><p><strong>Email Templates:</strong></p></li>
<li><p>Send mail to user after approve their report:</p></li>
</ul>
<table>
<colgroup>
<col style="width: 25%" />
<col style="width: 74%" />
</colgroup>
<tbody>
<tr>
<td><blockquote>
<p>From</p>
</blockquote></td>
<td><blockquote>
<p>techmarket@gmail.com</p>
</blockquote></td>
</tr>
<tr>
<td><blockquote>
<p>To</p>
</blockquote></td>
<td><blockquote>
<p>[user.email]</p>
</blockquote></td>
</tr>
<tr>
<td><blockquote>
<p>Cc</p>
</blockquote></td>
<td><blockquote>
<p>N/A</p>
</blockquote></td>
</tr>
<tr>
<td><blockquote>
<p>Subject</p>
</blockquote></td>
<td><blockquote>
<p>Get [Subject] of “Email Template” item of which [Keyword] = “Your
report has been approved”</p>
</blockquote></td>
</tr>
<tr>
<td><blockquote>
<p>Body</p>
</blockquote></td>
<td><blockquote>
<p>Get [Body] of “Email Template” item of which [Keyword] = “Your report
has been approved”</p>
</blockquote></td>
</tr>
</tbody>
</table>
<ul>
<li><p>Following is a sample email content:</p></li>
</ul>
<table>
<colgroup>
<col style="width: 25%" />
<col style="width: 74%" />
</colgroup>
<tbody>
<tr>
<td><blockquote>
<p>Subject</p>
</blockquote></td>
<td><blockquote>
<p>"Your report has been approved - Tech Market"</p>
</blockquote></td>
</tr>
<tr>
<td><blockquote>
<p>Body</p>
</blockquote></td>
<td><blockquote>
<p>[Body] = “Hello,”</p>
<p>[Body] = [Body] + 2 new lines</p>
<p>[Body] = [Body] + “Thanks for your report on the post
[post.title]!”</p>
<p>[Body] = [Body] + 2 new lines</p>
<p>[Body] = [Body] + “We have reviewed and confirmed the user
[user.username] violated our rules. Posts by this person will be taken
down.”</p>
<p>[Body] = [Body] + 2 new lines</p>
<p>[Body] = [Body] + "<mark>Thank you for your cooperation. Hope you
continue to accompany us.”</mark></p>
<p>[Body] = [Body] + 2 new lines</p>
<p>[Body] = [Body] + "<mark>Best regards,</mark>"</p>
<p>[Body] = [Body] + 2 new lines</p>
<p>[Body] = [Body] + "The <mark>Tech Market team</mark>"</p>
</blockquote></td>
</tr>
</tbody>
</table></td>
</tr>
<tr>
<td style="text-align: center;"><em>(8)</em></td>
<td style="text-align: center;"><em>BR119</em></td>
<td style="text-align: center;"><p><strong>Ban Rules:</strong></p>
<ul>
<li><p>[user.status] = BANNED</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(9)</em></td>
<td style="text-align: center;"><em>BR120</em></td>
<td style="text-align: center;"><p><strong>Sending email
rules:</strong></p>
<ul>
<li><p><strong>Email Templates:</strong></p></li>
<li><p>Send mail to user after change their account
status<strong>:</strong></p></li>
</ul>
<table>
<colgroup>
<col style="width: 25%" />
<col style="width: 74%" />
</colgroup>
<tbody>
<tr>
<td><blockquote>
<p>From</p>
</blockquote></td>
<td><blockquote>
<p>techmarket@gmail.com</p>
</blockquote></td>
</tr>
<tr>
<td><blockquote>
<p>To</p>
</blockquote></td>
<td><blockquote>
<p>[user.email]</p>
</blockquote></td>
</tr>
<tr>
<td><blockquote>
<p>Cc</p>
</blockquote></td>
<td><blockquote>
<p>N/A</p>
</blockquote></td>
</tr>
<tr>
<td><blockquote>
<p>Subject</p>
</blockquote></td>
<td><blockquote>
<p>Get [Subject] of “Email Template” item of which [Keyword] = “Account
violates regulations”</p>
</blockquote></td>
</tr>
<tr>
<td><blockquote>
<p>Body</p>
</blockquote></td>
<td><blockquote>
<p>Get [Body] of “Email Template” item of which [Keyword] = “Account
violates regulations”</p>
</blockquote></td>
</tr>
</tbody>
</table>
<ul>
<li><p>Following is a sample email content:</p></li>
</ul>
<table>
<colgroup>
<col style="width: 25%" />
<col style="width: 74%" />
</colgroup>
<tbody>
<tr>
<td><blockquote>
<p>Subject</p>
</blockquote></td>
<td><blockquote>
<p>"Account violates regulations - Tech Market"</p>
</blockquote></td>
</tr>
<tr>
<td><blockquote>
<p>Body</p>
</blockquote></td>
<td><blockquote>
<p>[Body] = “Hello,”</p>
<p>[Body] = [Body] + 2 new lines</p>
<p>[Body] = [Body] + “We found that your account violates our posting
rules.”</p>
<p>[Body] = [Body] + 2 new lines</p>
<p>[Body] = [Body] + “Report Details:”</p>
<p>[Body] = [Body] + 1 new lines</p>
<p>[Body] = [Body] + “- Reason: [report.reason]”</p>
<p>[Body] = [Body] + “- Details: [report.description]”</p>
<p>[Body] = [Body] + "<mark>If you have any questions or concerns,
please feel free to contact our customer support.</mark>"</p>
<p>[Body] = [Body] + 2 new lines</p>
<p>[Body] = [Body] + "<mark>Thank you for using our service!</mark>"</p>
<p>[Body] = [Body] + 2 new lines</p>
<p>[Body] = [Body] + "<mark>Best regards,</mark>"</p>
<p>[Body] = [Body] + 2 new lines</p>
<p>[Body] = [Body] + "The <mark>Tech Market team</mark>"</p>
</blockquote></td>
</tr>
</tbody>
</table></td>
</tr>
</tbody>
</table>

### 

### UC26: Manage TechMarket bank account

<table>
<colgroup>
<col style="width: 21%" />
<col style="width: 78%" />
</colgroup>
<tbody>
<tr>
<td><strong>Name</strong></td>
<td><strong>Manage TechMarket bank account</strong></td>
</tr>
<tr>
<td><strong>Description</strong></td>
<td>This use case describes how a user can manage a client's trading
currency storage account.</td>
</tr>
<tr>
<td><strong>Actor</strong></td>
<td>Admin</td>
</tr>
<tr>
<td><strong>Trigger</strong></td>
<td><ul>
<li><p>When the Admin wants to change TechMarket bank account.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Pre-condition</strong></td>
<td><ul>
<li><p>The Admin is logged in to the system.</p></li>
<li><p>The Admin accessed the bank account screen.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Post-condition</strong></td>
<td><ul>
<li><p>The bank account is changed.</p></li>
</ul></td>
</tr>
</tbody>
</table>

#### Activities Flow

<img src="media/image26.png" style="width:6.69272in;height:4.875in" />

#### Business Rules

<table>
<colgroup>
<col style="width: 14%" />
<col style="width: 12%" />
<col style="width: 73%" />
</colgroup>
<tbody>
<tr>
<td style="text-align: center;"><strong>Activity</strong></td>
<td style="text-align: center;"><strong>BR Code</strong></td>
<td style="text-align: center;"><strong>Description</strong></td>
</tr>
<tr>
<td style="text-align: center;"><em>(2)</em></td>
<td style="text-align: center;"><em>BR121</em></td>
<td style="text-align: center;"><p><strong>Loading Screen
Rules:</strong></p>
<ul>
<li><p>The system loads the “Bank account details” screen</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(3)</em></td>
<td style="text-align: center;"><em>BR122</em></td>
<td style="text-align: center;"><p><strong>Bank Rules:</strong></p>
<ul>
<li><p>Change the following entries:</p></li>
</ul>
<ul>
<li><p>[Bank] - select in a bank list</p></li>
<li><p>[Account_number]</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(5)</em></td>
<td style="text-align: center;"><em>BR123</em></td>
<td style="text-align: center;"><p><strong>Validating
rules:</strong></p>
<ul>
<li><p>Verify account information with the selected bank.</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(5.1)</em></td>
<td style="text-align: center;"><em>BR124</em></td>
<td style="text-align: center;"><p><strong>Saving Rules:</strong></p>
<ul>
<li><p>[bankAccount] = {</p></li>
</ul>
<blockquote>
<p>‘bank’ : &lt;&lt;selected bank&gt;&gt;,</p>
<p>‘account_number’: &lt;&lt;entered number&gt;&gt;</p>
<p>}</p>
</blockquote></td>
</tr>
<tr>
<td style="text-align: center;"><em>(5.2)</em></td>
<td style="text-align: center;"><em>BR125</em></td>
<td style="text-align: center;"><p><strong>Message Rules:</strong></p>
<ul>
<li><p>The system shows message MSG 53</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(6)</em></td>
<td style="text-align: center;"><em>BR126</em></td>
<td style="text-align: center;"><p><strong>Message Rules:</strong></p>
<ul>
<li><p>The system shows message MSG 52</p></li>
</ul></td>
</tr>
</tbody>
</table>

### UC27: Update legal documents

<table>
<colgroup>
<col style="width: 21%" />
<col style="width: 78%" />
</colgroup>
<tbody>
<tr>
<td><strong>Name</strong></td>
<td><strong>Update legal documents</strong></td>
</tr>
<tr>
<td><strong>Description</strong></td>
<td>This use case describes how users can update legal documents related
to a publicly available website.</td>
</tr>
<tr>
<td><strong>Actor</strong></td>
<td>Admin</td>
</tr>
<tr>
<td><strong>Trigger</strong></td>
<td><ul>
<li><p>When the Admin wants to change legal documents.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Pre-condition</strong></td>
<td><ul>
<li><p>The Admin is logged in to the system.</p></li>
<li><p>The Admin accessed the legal document screen.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Post-condition</strong></td>
<td><ul>
<li><p>The documents are changed.</p></li>
</ul></td>
</tr>
</tbody>
</table>

#### Activities Flow

<img src="media/image27.png" style="width:4.41302in;height:4.9522in" />

#### Business Rules

<table>
<colgroup>
<col style="width: 14%" />
<col style="width: 12%" />
<col style="width: 73%" />
</colgroup>
<tbody>
<tr>
<td style="text-align: center;"><strong>Activity</strong></td>
<td style="text-align: center;"><strong>BR Code</strong></td>
<td style="text-align: center;"><strong>Description</strong></td>
</tr>
<tr>
<td style="text-align: center;"><em>(3)</em></td>
<td style="text-align: center;"><em>BR127</em></td>
<td style="text-align: center;"><p><strong>Opening File
Rules:</strong></p>
<ul>
<li><p>Require the following file types: .doc, .docx, .pdf.</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(8)</em></td>
<td style="text-align: center;"><em>BR128</em></td>
<td style="text-align: center;"><p><strong>Saving Rules:</strong></p>
<ul>
<li><p>Replace document with the file you just uploaded.</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(9)</em></td>
<td style="text-align: center;"><em>BR129</em></td>
<td style="text-align: center;"><p><strong>Message Rules:</strong></p>
<ul>
<li><p>The system shows message MSG 54</p></li>
</ul></td>
</tr>
</tbody>
</table>

### UC28: Update information for the help center

<table>
<colgroup>
<col style="width: 21%" />
<col style="width: 78%" />
</colgroup>
<tbody>
<tr>
<td><strong>Name</strong></td>
<td><strong>Update information for the help center</strong></td>
</tr>
<tr>
<td><strong>Description</strong></td>
<td>This use case describes how users can update content and questions
for the help center.</td>
</tr>
<tr>
<td><strong>Actor</strong></td>
<td>Customer Care Department</td>
</tr>
<tr>
<td><strong>Trigger</strong></td>
<td><ul>
<li><p>When the Customer Care Department wants to update the help
center.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Pre-condition</strong></td>
<td><ul>
<li><p>The Customer Care Department is logged in to the system.</p></li>
<li><p>The Customer Care Department accessed the document
screen.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Post-condition</strong></td>
<td><ul>
<li><p>The documents are changed.</p></li>
</ul></td>
</tr>
</tbody>
</table>

#### Activities Flow

<img src="media/image28.png" style="width:5.20989in;height:5.89058in" />

#### Business Rules

<table>
<colgroup>
<col style="width: 14%" />
<col style="width: 12%" />
<col style="width: 73%" />
</colgroup>
<tbody>
<tr>
<td style="text-align: center;"><strong>Activity</strong></td>
<td style="text-align: center;"><strong>BR Code</strong></td>
<td style="text-align: center;"><strong>Description</strong></td>
</tr>
<tr>
<td style="text-align: center;"><em>(4)</em></td>
<td style="text-align: center;"><em>BR130</em></td>
<td style="text-align: center;"><p><strong>Opening File
Rules:</strong></p>
<ul>
<li><p>Require the following file types: .doc, .docx, .pdf.</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(9)</em></td>
<td style="text-align: center;"><em>BR131</em></td>
<td style="text-align: center;"><p><strong>Saving Rules:</strong></p>
<ul>
<li><p>If the user makes a change then replace the document with the
file you just uploaded.</p></li>
<li><p>If the user makes an addition then the file you just uploaded
will be saved.</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(10)</em></td>
<td style="text-align: center;"><em>BR132</em></td>
<td style="text-align: center;"><p><strong>Message Rules:</strong></p>
<ul>
<li><p>The system shows message MSG 54</p></li>
</ul></td>
</tr>
</tbody>
</table>

### UC29: Handle feedback from user

<table>
<colgroup>
<col style="width: 21%" />
<col style="width: 78%" />
</colgroup>
<tbody>
<tr>
<td><strong>Name</strong></td>
<td><strong>Handle feedback from user</strong></td>
</tr>
<tr>
<td><strong>Description</strong></td>
<td>This use case describes how users can view and respond to customer
feedback.</td>
</tr>
<tr>
<td><strong>Actor</strong></td>
<td>Customer Care Department</td>
</tr>
<tr>
<td><strong>Trigger</strong></td>
<td><ul>
<li><p>When the Customer Care Department wants to handle user
feedback.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Pre-condition</strong></td>
<td><ul>
<li><p>The Customer Care Department is logged in to the system.</p></li>
<li><p>The Customer Care Department accessed the feedback
screen.</p></li>
</ul></td>
</tr>
<tr>
<td><strong>Post-condition</strong></td>
<td><ul>
<li><p>The feedback is handled.</p></li>
</ul></td>
</tr>
</tbody>
</table>

#### Activities Flow

<img src="media/image29.png" style="width:4.23958in;height:3.96875in" />

#### Business Rules

<table>
<colgroup>
<col style="width: 14%" />
<col style="width: 12%" />
<col style="width: 73%" />
</colgroup>
<tbody>
<tr>
<td style="text-align: center;"><strong>Activity</strong></td>
<td style="text-align: center;"><strong>BR Code</strong></td>
<td style="text-align: center;"><strong>Description</strong></td>
</tr>
<tr>
<td style="text-align: center;"><em>(2)</em></td>
<td style="text-align: center;"><em>BR133</em></td>
<td style="text-align: center;"><p><strong>Loading Screen
Rules:</strong></p>
<ul>
<li><p>The system shows the “Feedback details” screen.</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(3)</em></td>
<td style="text-align: center;"><em>BR134</em></td>
<td style="text-align: center;"><p><strong>Handling Rules:</strong></p>
<ul>
<li><p>Users need to carefully consider feedback before clicking the
‘Handle’ button</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(4)</em></td>
<td style="text-align: center;"><em>BR135</em></td>
<td style="text-align: center;"><p><strong>Changing Rules:</strong></p>
<ul>
<li><p>[feedback.status] = HANDLED</p></li>
</ul></td>
</tr>
<tr>
<td style="text-align: center;"><em>(5)</em></td>
<td style="text-align: center;"><em>BR136</em></td>
<td style="text-align: center;"><p><strong>Message Rules:</strong></p>
<ul>
<li><p>The system shows message MSG 55</p></li>
</ul></td>
</tr>
</tbody>
</table>

# List Description

<u>TechMarket List Description.xlsx</u>

# View Description

[<u>TechMarket List
View.xlsx</u>](https://docs.google.com/spreadsheets/d/1kNTvdCvseMDWo9rzbmTGdMxM3unqoqlE/edit#gid=75550104)

> 

# Non-functional Requirements

## **3.1. User Access and Security** 

<table>
<colgroup>
<col style="width: 22%" />
<col style="width: 14%" />
<col style="width: 14%" />
<col style="width: 12%" />
<col style="width: 12%" />
<col style="width: 22%" />
</colgroup>
<tbody>
<tr>
<td style="text-align: right;"><p><strong>Actor</strong></p>
<p><strong>Function</strong></p></td>
<td style="text-align: center;"><strong>Admin</strong></td>
<td style="text-align: center;"><strong>User</strong></td>
<td style="text-align: center;"><strong>Buyer</strong></td>
<td style="text-align: center;"><strong>Seller</strong></td>
<td style="text-align: center;"><strong>Customer Care
Department</strong></td>
</tr>
<tr>
<td style="text-align: right;">Sign In</td>
<td style="text-align: center;">x</td>
<td style="text-align: center;">x</td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
</tr>
<tr>
<td style="text-align: right;">Sign Up</td>
<td style="text-align: center;"></td>
<td style="text-align: center;">x</td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
</tr>
<tr>
<td style="text-align: right;">Forgot password</td>
<td style="text-align: center;"></td>
<td style="text-align: center;">x</td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
</tr>
<tr>
<td style="text-align: right;">Leave a feedback</td>
<td style="text-align: center;"></td>
<td style="text-align: center;">x</td>
<td style="text-align: center;">x</td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
</tr>
<tr>
<td style="text-align: right;">Create post</td>
<td style="text-align: center;"></td>
<td style="text-align: center;">x</td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
</tr>
<tr>
<td style="text-align: right;">Update post</td>
<td style="text-align: center;"></td>
<td style="text-align: center;">x</td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
</tr>
<tr>
<td style="text-align: right;">Delete post</td>
<td style="text-align: center;"></td>
<td style="text-align: center;">x</td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
</tr>
<tr>
<td style="text-align: right;">Guarantee payment</td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
<td style="text-align: center;">x</td>
<td style="text-align: center;"></td>
</tr>
<tr>
<td style="text-align: right;">Link sales wallet</td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
<td style="text-align: center;">x</td>
<td style="text-align: center;"></td>
</tr>
<tr>
<td style="text-align: right;">Use post push service</td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
<td style="text-align: center;">x</td>
<td style="text-align: center;"></td>
</tr>
<tr>
<td style="text-align: right;">Confirm sales order</td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
<td style="text-align: center;">x</td>
<td style="text-align: center;"></td>
</tr>
<tr>
<td style="text-align: right;">Place order</td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
<td style="text-align: center;">x</td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
</tr>
<tr>
<td style="text-align: right;">Choose shipping address</td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
<td style="text-align: center;">x</td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
</tr>
<tr>
<td style="text-align: right;">Pay</td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
<td style="text-align: center;">x</td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
</tr>
<tr>
<td style="text-align: right;">Cancel order</td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
<td style="text-align: center;">x</td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
</tr>
<tr>
<td style="text-align: right;">Confirm delivery of order</td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
<td style="text-align: center;">x</td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
</tr>
<tr>
<td style="text-align: right;">Rate seller</td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
<td style="text-align: center;">x</td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
</tr>
<tr>
<td style="text-align: right;">Chat with seller</td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
<td style="text-align: center;">x</td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
</tr>
<tr>
<td style="text-align: right;">Report post</td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
<td style="text-align: center;">x</td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
</tr>
<tr>
<td style="text-align: right;">Create category</td>
<td style="text-align: center;">x</td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
</tr>
<tr>
<td style="text-align: right;">Update category name</td>
<td style="text-align: center;">x</td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
</tr>
<tr>
<td style="text-align: right;">Delete category</td>
<td style="text-align: center;">x</td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
</tr>
<tr>
<td style="text-align: right;">Approve post</td>
<td style="text-align: center;">x</td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
</tr>
<tr>
<td style="text-align: right;">Reject post</td>
<td style="text-align: center;">x</td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
</tr>
<tr>
<td style="text-align: right;">Ban account</td>
<td style="text-align: center;">x</td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
</tr>
<tr>
<td style="text-align: right;">Manage TechMarket bank account</td>
<td style="text-align: center;">x</td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
</tr>
<tr>
<td style="text-align: right;">Update legal documents</td>
<td style="text-align: center;">x</td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
</tr>
<tr>
<td style="text-align: right;">Update information for the help
centre</td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
<td style="text-align: center;">x</td>
</tr>
<tr>
<td style="text-align: right;">Handle feedback from user</td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
<td style="text-align: center;"></td>
<td style="text-align: center;">x</td>
</tr>
</tbody>
</table>

X: User has full permission to do the action.

## **3.2. Performance Requirements**

**Number of user**

- Number of concurrent user: 150

- Number of business user: 600 - 700

**Data volume**

- Number of documents: 6M – 8M file size

- Data growth rate: 5MB/ day

**Level of availability**

- 95%: Effective system management (assessed according to IBM standards,
  continuous operating time per year is no more than 18.25 days).

**Usage frequency**

- The system is used regularly, every hour there will be data exchanged
  between businesses and their supply partners. Therefore, the system
  needs to be set up on a server capable of operating throughout
  business hours. Upgrades, maintenance, and repairs only take place
  after hours.

## **3.3. Implementation Requirements**

**Location**

Ho Chi Minh city

**Read-only Duration**

1 day

**Read-only Timeframe**

0:00

**Maintenance Window**

Every week on Sunday evening at 11 p.m., lasting 1 to 2 hours. During
this time, programmers can take advantage of it to edit and update new
code

**Overall conversion timeline**

1st, 15th and 25th of every month

# Appendixes

## Glossary

The list below contains all the necessary terms to interpret the
document, including acronyms and abbreviations.

|  |  |
|----|----|
| **Term** | **Description** |
| *BR* | **B**usiness **R**ule |
| *CBR* | **C**ommon **B**usiness **R**ule |
| *DB* | Notes **D**ata**b**ase |
| *MSG* | **M**es**s**a**g**e |
| *UC* | **U**se **C**ase |
| *N/A* | **N**ot **A**vailable or **N**ot **A**pplicable, used to indicate when information in a certain section could not be provided because it does not apply to this application. |
| *UI* | **U**ser **I**nterface |
| *SRS* | **S**oftware **R**equirements **S**pecification |
| *TBD* | **T**o **b**e **d**etermined or **t**o **b**e **d**efined |

## 

## 

## Messages

This section describes the details of messages used in business rules
e.g. error messages, confirmation messages, etc.

|  |  |  |
|----|----|----|
| **Message Code** | **Message Content** | **Button** |
| MSG 1 | Are you certain with this decision? | OK/Cancel |
| MSG 2 | You need to fill in all fields |  |
| MSG 3 | Your post is ready to publish. Waiting for the Administrator. |  |
| MSG 4 | Payment failed. Please check your account. |  |
| MSG 5 | Payment success. |  |
| MSG 6 | Are you sure you have received your order? | OK/Cancel |
| MSG 7 | Are you certain to approve this post? |  |
| MSG 8 | Approve successful |  |
| MSG 9 | Are you certain to reject this post? |  |
| MSG 10 | Reject successful |  |
| MSG 11 | File size is too large |  |
| MSG 12 | Product does not exist |  |
| MSG 13 | Price can not be less than 0 |  |
| MSG 14 | Description must contain at least 50 characters |  |
| MSG 15 | You don’t have permission |  |
| MSG 16 | Your post has been updated. Waiting for the administrator's approval. |  |
| MSG 17 | Update post failed. |  |
| MSG 18 | The post does not exist. |  |
| MSG 19 | Can not delete the post that has been purchased. |  |
| MSG 20 | Delete post successfully. |  |
| MSG 21 | Delete post failed. |  |
| MSG 22 | Username or password is incorrect. |  |
| MSG 23 | Logged in successfully. |  |
| MSG 24 | Username must contain at least 8 characters. |  |
| MSG 25 | Invalid password |  |
| MSG 26 | Phone number has been used. |  |
| MSG 27 | Email has been used. |  |
| MSG 28 | Successfully registered. |  |
| MSG 29 | Phone/email already exists. |  |
| MSG 30 | Invalid phone number. |  |
| MSG 31 | Invalid email |  |
| MSG 32 | User not found. |  |
| MSG 33 | Invalid verification link. |  |
| MSG 34 | Must not be the same as the old password |  |
| MSG 35 | The post does not exist. |  |
| MSG 36 | The message contains impolite words. |  |
| MSG 37 | Invalid payment information. |  |
| MSG 38 | Payment success. |  |
| MSG 39 | Payment failed. |  |
| MSG 40 | Confirm order successfully. |  |
| MSG 41 | Rating buyer failed. |  |
| MSG 42 | Thanks for your rating. |  |
| MSG 43 | This post has been updated with 'Guarantee Payment' |  |
| MSG 44 | You need to link to your sales wallet first! | Link/Cancel |
| MSG 45 | Phone number is not valid |  |
| MSG 46 | OTP is incorrect |  |
| MSG 47 | Thank you for your report. We will review and send the verification results to the email you provide. |  |
| MSG 48 | Your new category has been created. |  |
| MSG 49 | The category name already exists |  |
| MSG 50 | Category update successful |  |
| MSG 51 | ALERT. Do you want to delete these categories? This action can not be undone. | OK/Cancel |
| MSG 52 | Account change successful |  |
| MSG 53 | This account is Invalid. |  |
| MSG 54 | Document was updated successfully. |  |
| MSG 55 | Handle feedback successful |  |

## Issues List

N/A
