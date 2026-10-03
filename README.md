# EMV & ISO 8583 Protocol Master Guide & Interactive Simulator Application

An ultra-comprehensive, production-grade, pure Java Android reference application and educational textbook covering the complete specifications, cryptography, message formats, byte structures, and end-to-end transaction flows for **EMV (Chip & Contactless Smart Card Standard)** and **ISO 8583 (Financial Transaction Card Originated Messages)** protocols.

---

## Table of Contents

1. [Project Overview & Executive Summary](#1-project-overview--executive-summary)
2. [EMV Protocol Deep-Dive Specification](#2-emv-protocol-deep-dive-specification)
   - [2.1 What is EMV?](#21-what-is-emv)
   - [2.2 ISO 7816 APDU Command & Response Format](#22-iso-7816-apdu-command--response-format)
   - [2.3 BER-TLV Data Encoding Structure](#23-ber-tlv-data-encoding-structure)
   - [2.4 Comprehensive EMV Tag Dictionary](#24-comprehensive-emv-tag-dictionary)
   - [2.5 Terminal Verification Results (TVR - Tag 95) Bitmask Reference](#25-terminal-verification-results-tvr---tag-95-bitmask-reference)
   - [2.6 Application Interchange Profile (AIP - Tag 82) Reference](#26-application-interchange-profile-aip---tag-82-reference)
   - [2.7 Cryptogram Information Data (CID - Tag 9F27) & CVM Results](#27-cryptogram-information-data-cid---tag-9f27--cvm-results)
   - [2.8 Offline Data Authentication (SDA, DDA, CDA)](#28-offline-data-authentication-sda-dda-cda)
   - [2.9 The 10-Step EMV Transaction Lifecycle](#29-the-10-step-emv-transaction-lifecycle)
   - [2.10 EMV Cryptography & Session Key Derivation](#210-emv-cryptography--session-key-derivation)
3. [ISO 8583 Protocol Deep-Dive Specification](#3-iso-8583-protocol-deep-dive-specification)
   - [3.1 What is ISO 8583?](#31-what-is-iso-8583)
   - [3.2 ISO 8583 Message Anatomy](#32-iso-8583-message-anatomy)
   - [3.3 Message Type Identifier (MTI) Decoding Rules](#33-message-type-identifier-mti-decoding-rules)
   - [3.4 Primary & Secondary Bitmaps Calculation Algorithm](#34-primary--secondary-bitmaps-calculation-algorithm)
   - [3.5 ISO 8583 Data Element Catalog (Fields 1 to 128)](#35-iso-8583-data-element-catalog-fields-1-to-128)
   - [3.6 ISO 9564-1 PIN Block Formats & XOR Calculations](#36-iso-9564-1-pin-block-formats--xor-calculations)
4. [EMV & ISO 8583 Integration: Field 55 (ICC System Related Data)](#4-emv--iso-8583-integration-field-55-icc-system-related-data)
   - [4.1 Field 55 Request TLV Container](#41-field-55-request-tlv-container)
   - [4.2 Field 55 Response TLV Container](#42-field-55-response-tlv-container)
5. [End-to-End Payment Gateway Transaction Flow Sequence](#5-end-to-end-payment-gateway-transaction-flow-sequence)
   - [5.1 Interactive Gateway Sequence Diagram](#51-interactive-gateway-sequence-diagram)
   - [5.2 Byte-by-Byte Raw Hex Stream Breakdown](#52-byte-by-byte-raw-hex-stream-breakdown)
6. [Application Architecture & Java Package Map](#6-application-architecture--java-package-map)
7. [Unit Testing & Verification Strategy](#7-unit-testing--verification-strategy)
8. [Build & Installation Guide](#8-build--installation-guide)

---

## 1. Project Overview & Executive Summary

This application is designed as an all-in-one educational textbook, protocol analyzer, message packer/unpacker, APDU inspector, and interactive payment gateway simulator for banking software engineers, POS terminal developers, payment system architects, and cybersecurity researchers.

### Key Capabilities & Built-in Tools:
- **Pure Java BER-TLV Parser & Engine**: Parses complex nested BER-TLV structures with multi-byte tags and length specifiers. Automatically generates line-by-line bitwise breakdowns for TVR (Tag 95), AIP (Tag 82), CID (Tag 9F27), and CVM Results (Tag 9F34).
- **ISO 7816 / EMV APDU Command Generator & Inspector**: Interactive APDU command builder supporting `SELECT PSE/PPSE`, `SELECT AID` (Visa, Mastercard, Troy, Amex), `GPO`, `READ RECORD`, `VERIFY PIN`, and `1st/2nd GENERATE AC`. Includes a complete SW1 SW2 Status Word dictionary lookup.
- **ISO 8583 Packer & Unpacker Engine**: Full compliance with ISO 8583-1:1987 standard for message classes `0100`, `0110`, `0200`, `0210`, `0420`, and `0800`. Features real-time primary (64-bit) and secondary (128-bit) bitmap calculation, MTI decoding, and Field 55 EMV payload extraction.
- **ISO 9564-1 PIN Block & Cryptography Simulator**: Calculates ISO-0 (ANSI X9.8) and Format 1 PIN blocks with step-by-step XOR visuals. Demonstrates EMV Session Key Derivation (using ATC + Master Key) and EMV Book 2 Table 13 ARQC input vector generation with Triple-DES CBC-MAC signatures.
- **End-to-End Payment Gateway Flow Simulator**: Live, interactive simulation connecting POS Terminal APDU exchanges with EMV Chip Cards, Acquirer ISO 8583 `0200` message packing, Bank Host authorization processing, ISO 8583 `0210` response generation, and 2nd Generate AC (TC approval) card write-back.

---

## 2. EMV Protocol Deep-Dive Specification

### 2.1 What is EMV?

**EMV** stands for **Eurocard, Mastercard, and Visa**, the three payment organizations that initially created the global standard for smart card payment transactions. Managed today by EMVCo (owned by Visa, Mastercard, JCB, American Express, China UnionPay, and Discover), the specification defines the interaction between IC (Integrated Circuit) smart card payment applications and terminal devices (POS, ATM, mPOS).

Unlike legacy magnetic stripe cards that store static, easily cloned cardholder data (Track 1 / Track 2), EMV smart cards contain a secure tamper-resistant microcontroller capable of executing cryptographic algorithms (DES/3DES/AES, RSA, ECC). During every EMV transaction, the card generates a unique, one-time cryptographic signature called an **Application Cryptogram (AC)**, preventing replay attacks and card cloning.

---

### 2.2 ISO 7816 APDU Command & Response Format

The interface between the terminal terminal device (CAD - Card Acceptance Device) and the smart card follows the **ISO/IEC 7816-4** Application Protocol Data Unit (APDU) standard.

#### APDU Command Structure (C-APDU)
A command APDU sent by the terminal consists of a mandatory 4-byte header followed by optional data lengths and payload bytes:

| Field Name | Length | Description |
| :--- | :--- | :--- |
| **CLA** | 1 Byte | Command Class (e.g., `00` for ISO 7816 standard, `80` for EMV proprietary) |
| **INS** | 1 Byte | Instruction Code (e.g., `A4` SELECT, `A8` GPO, `B2` READ RECORD, `AE` GENERATE AC) |
| **P1** | 1 Byte | Parameter 1 (e.g., Selection option or Record number) |
| **P2** | 1 Byte | Parameter 2 (e.g., SFI shifted by 3 bits or reference control parameter) |
| **Lc** | 0 or 1 Byte | Length of Command Data field |
| **Data** | Lc Bytes | Command payload bytes (e.g., AID, PDOL data, CDOL1 data) |
| **Le** | 0 or 1 Byte | Maximum expected response length (`00` indicates up to 256 bytes) |

#### APDU Response Structure (R-APDU)
A response APDU returned by the card consists of an optional data payload followed by a mandatory 2-byte Status Word (**SW1 SW2**):

| Field Name | Length | Description |
| :--- | :--- | :--- |
| **Data** | N Bytes | Response data returned by card (e.g., FCI template, GPO response, Record TLVs) |
| **SW1** | 1 Byte | Status Word 1 (e.g., `90` Normal completion, `61` Response bytes available, `6A` Error) |
| **SW2** | 1 Byte | Status Word 2 (e.g., `00` Success, `82` File not found, `C2` PIN failed) |

#### Common EMV APDU Status Words (SW1 SW2):
- `9000`: Process completed successfully (OK).
- `61XX`: Command successful; `XX` additional bytes of response data available (issue `GET RESPONSE`).
- `63C2`: PIN Verification Failed; 2 attempts remaining.
- `63C1`: PIN Verification Failed; 1 attempt remaining.
- `63C0`: PIN Verification Failed; PIN Try Limit Exceeded (Card Blocked).
- `6700`: Wrong length (incorrect Lc/Le).
- `6982`: Security status not satisfied.
- `6985`: Conditions of use not satisfied.
- `6A81`: Function not supported.
- `6A82`: File or Application not found (e.g. invalid AID or PSE).
- `6A83`: Record not found.

---

### 2.3 BER-TLV Data Encoding Structure

EMV uses **BER-TLV (Basic Encoding Rules - Tag Length Value)** per ISO/IEC 8825-1 to format all card and terminal data fields.

```
+--------------------------------+--------------------------+-----------------------+
|          TAG FIELD             |      LENGTH FIELD        |      VALUE FIELD      |
|  1 to 3 Bytes (BER Encoding)   |  1 to 3 Bytes (Var Length)|     N Bytes Payload   |
+--------------------------------+--------------------------+-----------------------+
```

#### 1. Tag Field Encoding Rules:
- **Byte 1 Bit 8-7**: Tag Class
  - `00`: Universal Class
  - `01`: Application Class
  - `10`: Context-Specific Class
  - `11`: Private Class
- **Byte 1 Bit 6**: Tag Type
  - `0`: Primitive (contains raw data value)
  - `1`: Constructed (contains nested child TLVs)
- **Byte 1 Bit 5-1**: Tag Number
  - If Bits 5-1 are NOT all `1`s (`11111` / `0x1F`), the tag is 1 byte long (e.g., `82`, `95`, `4F`, `50`).
  - If Bits 5-1 ARE all `1`s (`0x1F`), the tag continues into subsequent bytes until a byte with Bit 8 = `0` is found (e.g., `9F26`, `9F27`, `5F2A`, `BF0C`).

#### 2. Length Field Encoding Rules:
- **Single-Byte Length**: If Bit 8 = `0`, the value length is stored in Bits 7-1 (0 to 127 bytes). Example: `06` = 6 bytes length.
- **Multi-Byte Length**: If Bit 8 = `1`, Bits 7-1 indicate the number of subsequent length bytes.
  - Example `81 80`: `81` indicates 1 subsequent length byte -> `80` = 128 bytes length.
  - Example `82 01 20`: `82` indicates 2 subsequent length bytes -> `01 20` = 288 bytes length.

---

### 2.4 Comprehensive EMV Tag Dictionary

Below is a reference catalog of major EMV tags implemented in `EmvTagDictionary.java`:

| Tag Hex | Tag Name | Format | Class / Type | Description |
| :--- | :--- | :--- | :--- | :--- |
| **4F** | Application Identifier (AID) | b 40-128 | Application / Prim | Identifies the card application (e.g. Visa `A0000000031010`) |
| **50** | Application Label | ans 1-16 | Application / Prim | User-readable application name stored on card |
| **57** | Track 2 Equivalent Data | b | Application / Prim | Contains PAN, Expiration Date, Service Code formatted as Track 2 |
| **5A** | Application PAN | cn 1-19 | Application / Prim | Primary Account Number (Card Number) |
| **5F20** | Cardholder Name | ans 2-26 | Application / Prim | Name of the cardholder |
| **5F24** | Application Expiration Date | n 6 | Application / Prim | Expiry date in `YYMMDD` format |
| **5F25** | Application Effective Date | n 6 | Application / Prim | Valid-from date in `YYMMDD` format |
| **5F2A** | Transaction Currency Code | n 3 | Application / Prim | ISO 4217 numeric currency code (e.g. `0792` TRY, `0840` USD) |
| **5F34** | PAN Sequence Number (PSN) | n 2 | Application / Prim | Differentiates cards issued under same PAN |
| **6F** | FCI Template | b | Universal / Const | File Control Information template returned on SELECT |
| **70** | READ RECORD Response | b | Universal / Const | Template containing record data read from card SFI |
| **71** | Issuer Script Template 1 | b | Universal / Const | Proprietary script commands executed before 2nd Generate AC |
| **72** | Issuer Script Template 2 | b | Universal / Const | Proprietary script commands executed after 2nd Generate AC |
| **77** | Response Template Format 2 | b | Universal / Const | Explicit TLVs returned in GPO / Generate AC responses |
| **80** | Response Template Format 1 | b | Universal / Prim | Concatenated AIP and AFL bytes returned in GPO |
| **82** | Application Interchange Profile | b 16 | Application / Prim | 2-byte capability bit field indicating card security features |
| **84** | Dedicated File (DF) Name | b 40-128 | Application / Prim | AID of selected file |
| **8A** | Authorization Response Code | an 2 | Application / Prim | ISO 8583 response code returned by host (`00` Approved) |
| **8C** | CDOL1 | b | Application / Prim | List of terminal tags required in 1st Generate AC |
| **8D** | CDOL2 | b | Application / Prim | List of terminal tags required in 2nd Generate AC |
| **8E** | CVM List | b | Application / Prim | List of CVM rules and conditions evaluated by terminal |
| **90** | Issuer PK Certificate | b | Application / Prim | Certificate used in SDA/DDA offline authentication |
| **91** | Issuer Authentication Data | b 64-128 | Application / Prim | ARPC cryptogram returned by host to authenticate host to card |
| **94** | Application File Locator (AFL) | b | Application / Prim | Defines SFI and record ranges to read during transaction |
| **95** | Terminal Verification Results | b 40 | Application / Prim | 5-byte bit field storing security and risk status flags |
| **9A** | Transaction Date | n 6 | Application / Prim | Local date in `YYMMDD` format |
| **9C** | Transaction Type | n 2 | Application / Prim | Transaction intent (`00` Purchase, `01` Cash, `20` Refund) |
| **9F02** | Amount, Authorised (Numeric) | n 12 | Application / Prim | Authorized amount in smallest currency units (cents) |
| **9F03** | Amount, Other (Numeric) | n 12 | Application / Prim | Cashback or secondary amount |
| **9F10** | Issuer Application Data (IAD) | b | Application / Prim | Proprietary card issuer verification and CVR data |
| **9F1A** | Terminal Country Code | n 3 | Application / Prim | ISO 3166-1 numeric country code (`0792` Turkey) |
| **9F26** | Application Cryptogram (AC) | b 64 | Application / Prim | 8-byte cryptographic hash generated by card (ARQC / TC / AAC) |
| **9F27** | Cryptogram Info Data (CID) | b 8 | Application / Prim | Cryptogram type (`80` ARQC, `40` TC, `00` AAC) |
| **9F33** | Terminal Capabilities | b 24 | Application / Prim | 3-byte hardware capabilities of terminal |
| **9F34** | CVM Results | b 24 | Application / Prim | 3-byte result of CVM processing performed by terminal |
| **9F36** | Application Transaction Counter | b 16 | Application / Prim | Monotonically increasing counter incremented on every transaction |
| **9F37** | Unpredictable Number (UN) | b 32 | Application / Prim | 4-byte random challenge generated by terminal |
| **9F66** | Terminal Qualifiers (TTQ) | b 24 | Application / Prim | Contactless transaction capabilities indicator |

---

### 2.5 Terminal Verification Results (TVR - Tag 95) Bitmask Reference

Tag **95** is a 5-byte (40-bit) binary field maintained by the terminal to track errors, risk checks, and authentication failures throughout the transaction:

```
Byte 1: Offline Data Authentication Status
  Bit 8 [0x80]: Offline data authentication was not performed
  Bit 7 [0x40]: SDA failed
  Bit 6 [0x20]: ICC data missing
  Bit 5 [0x10]: Card appears on terminal exception list (Blacklist)
  Bit 4 [0x08]: DDA failed
  Bit 3 [0x04]: CDA failed
  Bit 2-1     : RFU

Byte 2: Application / Expiry Status
  Bit 8 [0x80]: ICC and terminal application versions differ
  Bit 7 [0x40]: Expired application
  Bit 6 [0x20]: Application not yet effective
  Bit 5 [0x10]: Requested service not allowed for card product
  Bit 4 [0x08]: New card
  Bit 3-1     : RFU

Byte 3: Cardholder Verification (CVM) Status
  Bit 8 [0x80]: Cardholder verification was not successful
  Bit 7 [0x40]: Unrecognised CVM
  Bit 6 [0x20]: PIN Try Limit exceeded
  Bit 5 [0x10]: PIN entry required and PIN pad not present or not working
  Bit 4 [0x08]: PIN entry required, PIN pad present, but PIN was not entered
  Bit 3 [0x04]: Online PIN entered
  Bit 2-1     : RFU

Byte 4: Terminal Risk Management Status
  Bit 8 [0x80]: Transaction exceeds floor limit
  Bit 7 [0x40]: Lower consecutive offline limit exceeded
  Bit 6 [0x20]: Upper consecutive offline limit exceeded
  Bit 5 [0x10]: Transaction selected randomly for online processing
  Bit 4 [0x08]: Merchant forced transaction online
  Bit 3-1     : RFU

Byte 5: Issuer Action & Script Status
  Bit 8 [0x80]: Default TDOL used
  Bit 7 [0x40]: Issuer authentication failed
  Bit 6 [0x20]: Script processing failed before 1st GENERATE AC
  Bit 5 [0x10]: Script processing failed after 1st GENERATE AC
  Bit 4-1     : RFU
```

---

### 2.6 Application Interchange Profile (AIP - Tag 82) Reference

Tag **82** is a 2-byte bitmask returned by the card in the GPO response indicating supported features:

```
Byte 1:
  Bit 8 [0x80]: Static Data Authentication (SDA) Supported
  Bit 7 [0x40]: Dynamic Data Authentication (DDA) Supported
  Bit 6 [0x20]: Cardholder Verification (CVM) Supported
  Bit 5 [0x10]: Terminal Risk Management Required
  Bit 4 [0x08]: Issuer Authentication Supported
  Bit 3 [0x04]: On-card cardholder verification supported
  Bit 2 [0x02]: CDA Supported
  Bit 1 [0x01]: RFU

Byte 2:
  Bit 8 [0x80]: EMV Contactless / Mobile Supported
  Bit 7-1     : RFU
```

---

### 2.7 Cryptogram Information Data (CID - Tag 9F27) & CVM Results

Tag **9F27** (CID) specifies the type of cryptogram returned by the card:

| CID Hex | Cryptogram Type | Meaning | Next Step |
| :--- | :--- | :--- | :--- |
| **80** | **ARQC** (Authorisation Request Cryptogram) | Online authorization required | Send ISO 8583 `0200` to Host |
| **40** | **TC** (Transaction Certificate) | Offline approved | Transaction complete offline |
| **00** | **AAC** (Application Authentication Cryptogram) | Offline declined | Transaction aborted offline |

Tag **9F34** (CVM Results - 3 Bytes):
- **Byte 1**: CVM Performed (`01` Plaintext PIN, `02` Online PIN, `1E` Signature, `1F` No CVM, `22` CDCVM).
- **Byte 2**: CVM Condition (`00` Always, `01` If unattended cash, `03` If terminal supports CVM).
- **Byte 3**: Result (`01` Failed, `02` Successful).

---

### 2.8 Offline Data Authentication (SDA, DDA, CDA)

Offline Data Authentication verifies that the smart card is authentic and that card data has not been altered:

1. **Static Data Authentication (SDA)**: Uses a static RSA signature signed by the CA (Certification Authority) over card static data (PAN, Expiry). It protects against data alteration but does NOT protect against card cloning.
2. **Dynamic Data Authentication (DDA)**: The card contains an internal RSA private key and generates a dynamic RSA signature over the Unpredictable Number (Tag `9F37`) supplied by the terminal. This guarantees the card is authentic and unclonable.
3. **Combined DDA / AC Generation (CDA)**: Combines DDA signature generation with the First/Second Generate AC step, preventing man-in-the-middle attacks on terminal-to-card APDU exchanges.

---

### 2.9 The 10-Step EMV Transaction Lifecycle

```
+-----------------------------------------------------------------------------------+
|                           1. APPLICATION SELECTION                                 |
|  Terminal reads PSE (12PAY.SYS.DDF01) or PPSE (2PAY.SYS.DDF01) to discover AIDs. |
+-----------------------------------------------------------------------------------+
                                         |
                                         v
+-----------------------------------------------------------------------------------+
|                     2. INITIATE APPLICATION PROCESSING                            |
|  Terminal sends GPO (Get Processing Options) -> Card returns AIP (82) & AFL (94).|
+-----------------------------------------------------------------------------------+
                                         |
                                         v
+-----------------------------------------------------------------------------------+
|                            3. READ APPLICATION DATA                               |
|  Terminal reads SFI records defined in AFL to get PAN, Expiry, Track 2, Certs.   |
+-----------------------------------------------------------------------------------+
                                         |
                                         v
+-----------------------------------------------------------------------------------+
|                        4. OFFLINE DATA AUTHENTICATION                             |
|  Terminal verifies SDA, DDA, or CDA RSA signatures using CA Public Keys.           |
+-----------------------------------------------------------------------------------+
                                         |
                                         v
+-----------------------------------------------------------------------------------+
|                           5. PROCESSING RESTRICTIONS                              |
|  Terminal checks Application Version (9F08/9F09), Expiry Date, and AUC (9F07).   |
+-----------------------------------------------------------------------------------+
                                         |
                                         v
+-----------------------------------------------------------------------------------+
|                         6. CARDHOLDER VERIFICATION (CVM)                           |
|  Evaluates CVM List (8E): Online PIN, Offline Plaintext PIN, CDCVM, or Signature.  |
+-----------------------------------------------------------------------------------+
                                         |
                                         v
+-----------------------------------------------------------------------------------+
|                       7. TERMINAL RISK MANAGEMENT & TVR                            |
|  Evaluates Floor Limits, Random Online Selection, and updates TVR (Tag 95).       |
+-----------------------------------------------------------------------------------+
                                         |
                                         v
+-----------------------------------------------------------------------------------+
|                         8. FIRST GENERATE AC (ARQC)                               |
|  Terminal requests ARQC (0x80). Card generates ARQC (9F26) & ATC (9F36).          |
+-----------------------------------------------------------------------------------+
                                         |
                                         v
+-----------------------------------------------------------------------------------+
|                  9. ONLINE AUTHORIZATION & FIELD 55 INTEGRATION                    |
|  POS constructs ISO 8583 0200 with Field 55. Host returns 0210 with Tag 91 ARPC. |
+-----------------------------------------------------------------------------------+
                                         |
                                         v
+-----------------------------------------------------------------------------------+
|                       10. SECOND GENERATE AC (COMPLETION)                         |
|  Terminal sends Tag 91 (ARPC) & 8A (ARC) to card via 2nd Generate AC -> TC (40).  |
+-----------------------------------------------------------------------------------+
```

---

### 2.10 EMV Cryptography & Session Key Derivation

Every EMV card possesses a unique **Master Key (MK)** shared with the Issuer Host (or derived via Issuer Master Key + PAN + PSN diversification).

#### 1. Session Key Derivation (SK):
To ensure every transaction uses unique keys, the card derives a temporary **Session Key (SK)** using the 16-bit **Application Transaction Counter (ATC - Tag 9F36)**:
$$\text{Session Key (SK)} = \text{3DES}_{\text{MK}}(\text{ATC} \parallel \text{0x00000000000000F0})$$

#### 2. ARQC Input Vector Construction (EMV Book 2 Table 13):
The card concatenates the following fields into a 34-byte binary vector:
```
Vector = Amount Authorized (9F02 - 6B)
       || Amount Other (9F03 - 6B)
       || Terminal Country Code (9F1A - 2B)
       || Terminal Verification Results (95 - 5B)
       || Transaction Currency Code (5F2A - 2B)
       || Transaction Date (9A - 3B)
       || Transaction Type (9C - 1B)
       || Unpredictable Number (9F37 - 4B)
       || Application Interchange Profile (82 - 2B)
       || Application Transaction Counter (9F36 - 2B)
       || Issuer Application Data (9F10 - Var)
```

#### 3. Cryptogram Calculation:
The card calculates an 8-byte CBC-MAC signature over the Input Vector using the Session Key:
$$\text{ARQC (Tag 9F26)} = \text{3DES-CBC-MAC}_{\text{SK}}(\text{Input Vector})$$

---

## 3. ISO 8583 Protocol Deep-Dive Specification

### 3.1 What is ISO 8583?

**ISO 8583** (*Financial transaction card originated messages — Interchange message specifications*) is the international standard governing financial messaging between point-of-sale (POS) terminals, automated teller machines (ATMs), merchant acquiring processors, payment card networks (Visa BASE I / SMS, Mastercard CIS / IPM, Troy), and issuing bank hosts.

---

### 3.2 ISO 8583 Message Anatomy

An ISO 8583 message stream consists of four consecutive structural layers:

```
+------------------+-------------------+--------------------+-----------------------+
|  HEADER (TPDU)   |    MTI FIELD      |   PRIMARY BITMAP   |     DATA ELEMENTS     |
| Optional 5-B Hex |   4 ASCII Digits  |  8 Bytes / 16 Hex  |  Fields 2 to 64/128   |
+------------------+-------------------+--------------------+-----------------------+
```

---

### 3.3 Message Type Identifier (MTI) Decoding Rules

The **MTI** is a 4-digit numeric code specifying the high-level intent of the message:

```
Digit 1: ISO Version
  0 : ISO 8583-1:1987 Standard
  1 : ISO 8583-2:1993 Standard
  2 : ISO 8583-3:2003 Standard

Digit 2: Message Class
  1 : Authorization Message
  2 : Financial Message (Purchase, Cash, Refund)
  3 : File Action Message
  4 : Reversal / Chargeback Message
  5 : Settlement / Reconciliation Message
  8 : Network Management Message (Echo Test, Logon)

Digit 3: Message Function
  0 : Request (İstek)
  1 : Response (Yanıt)
  2 : Advice (Tavsiye)
  3 : Advice Response
  4 : Notification

Digit 4: Message Origin
  0 : Acquirer (Anlaşmalı Banka)
  1 : Acquirer Repeat
  2 : Issuer (Kart İhraç Eden Banka)
  3 : Issuer Repeat
```

#### Common MTI Examples:
- `0100`: Authorization Request (POS -> Host)
- `0110`: Authorization Response (Host -> POS)
- `0200`: Financial Purchase Request (POS -> Host)
- `0210`: Financial Purchase Response (Host -> POS)
- `0420`: Reversal Advice (Timeout Cancellation)
- `0430`: Reversal Advice Response
- `0800`: Network Management Echo Test Request
- `0810`: Network Management Echo Test Response

---

### 3.4 Primary & Secondary Bitmaps Calculation Algorithm

An ISO 8583 message uses a bitmask (Bitmap) to indicate which of the 128 possible fields are present in the payload, avoiding empty padded fields.

- **Primary Bitmap**: 8 bytes (64 bits / 16 Hex characters). Controls presence of Fields 1 to 64.
- **Secondary Bitmap**: 8 bytes (64 bits / 16 Hex characters). Controls presence of Fields 65 to 128.
- **Rule**: **Field 1** is the Secondary Bitmap flag. If Field 1 is bit `1`, a Secondary Bitmap is appended immediately following the Primary Bitmap.

#### Bitmask Calculation Formula:
For field number $N$ ($1 \le N \le 128$):
$$\text{Byte Index} = \lfloor \frac{N - 1}{8} \rfloor, \quad \text{Bit Position within Byte} = 7 - ((N - 1) \bmod 8)$$

---

### 3.5 ISO 8583 Data Element Catalog (Fields 1 to 128)

Below is the standard field catalog implemented in `IsoFieldDictionary.java`:

| Field | Field Name | Data Type | Length Type | Max Len | Description |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **1** | Secondary Bitmap | BINARY | FIXED | 8 B | Indicates presence of Fields 65-128 |
| **2** | Primary Account Number (PAN) | NUMERIC | LLVAR | 19 | Card Number |
| **3** | Processing Code | NUMERIC | FIXED | 6 N | Transaction type (`000000` Purchase) |
| **4** | Amount, Transaction | NUMERIC | FIXED | 12 N | Transaction amount in cents |
| **7** | Transmission Date & Time | NUMERIC | FIXED | 10 N | Transmission time in `MMDDhhmmss` |
| **11** | Systems Trace Audit Number (STAN) | NUMERIC | FIXED | 6 N | Unique trace number generated by POS |
| **12** | Time, Local Transaction | NUMERIC | FIXED | 6 N | Local time in `hhmmss` |
| **13** | Date, Local Transaction | NUMERIC | FIXED | 4 N | Local date in `MMDD` |
| **14** | Date, Expiration | NUMERIC | FIXED | 4 N | Card expiration in `YYMM` |
| **18** | Merchant Category Code (MCC) | NUMERIC | FIXED | 4 N | Merchant sector classification code |
| **22** | Point of Service Entry Mode | NUMERIC | FIXED | 3 N | Card entry mode (`021` Magstripe, `051` EMV Chip, `071` Contactless) |
| **23** | Card Sequence Number (PSN) | NUMERIC | FIXED | 3 N | Card sequence number |
| **25** | POS Condition Code | NUMERIC | FIXED | 2 N | POS environment (`00` Normal customer present) |
| **32** | Acquiring Institution ID | NUMERIC | LLVAR | 11 N | Acquirer bank ID |
| **35** | Track 2 Data | TRACK2 | LLVAR | 37 Z | Magnetic track 2 data |
| **37** | Retrieval Reference Number (RRN) | ALPHANUMERIC | FIXED | 12 AN | Unique transaction reference number |
| **38** | Auth Identification Response | ALPHANUMERIC | FIXED | 6 AN | 6-digit approval authorization code |
| **39** | Response Code | ALPHANUMERIC | FIXED | 2 AN | Host response code (`00` Approved, `51` Insufficient Funds) |
| **41** | Card Acceptor Terminal ID (TID) | ALPHANUMERIC | FIXED | 8 AN | POS Terminal ID |
| **42** | Card Acceptor ID Code (MID) | ALPHANUMERIC | FIXED | 15 AN | Merchant ID |
| **43** | Card Acceptor Name/Location | ALPHANUMERIC | FIXED | 40 AN | Merchant name and address |
| **48** | Private Additional Data | SPECIAL | LLLVAR | 999 ANS | Proprietary private data field |
| **52** | Personal Identification Number | BINARY | FIXED | 8 B | Encrypted PIN Block (ISO 9564-1) |
| **54** | Additional Amounts | ALPHANUMERIC | LLLVAR | 120 ANS | Balance amounts or bonus points |
| **55** | ICC System Related Data | BINARY | LLLVAR | 255 B | **EMV Data TLV Container** |
| **62** | Private Reserved | SPECIAL | LLLVAR | 999 ANS | Proprietary transaction identifier |
| **64** | Message Authentication Code | BINARY | FIXED | 8 B | MAC signature |
| **70** | Network Management Code | NUMERIC | FIXED | 3 N | Network management code (`301` Echo Test) |

---

### 3.6 ISO 9564-1 PIN Block Formats & XOR Calculations

ISO 8583 Field 52 contains the encrypted PIN Block formatted according to **ISO 9564-1**.

#### ISO 9564-1 Format 0 (ISO-0 / ANSI X9.8):
$$\text{PIN Block} = \text{Clear PIN Field} \oplus \text{PAN Field}$$

1. **Clear PIN Field (16 Hex Nibbles / 8 Bytes)**:
   - Format: `0` + `L` (PIN length 4 to 12) + PIN digits + padded `F`s.
   - Example for PIN `1234`: `041234FFFFFFFFFF`

2. **PAN Field (16 Hex Nibbles / 8 Bytes)**:
   - Format: `0000` + rightmost 12 digits of PAN excluding check digit.
   - Example for PAN `4543123456789012`: `0000312345678901`

3. **XOR Step**:
```
  041234FFFFFFFFFF  (Clear PIN Field)
X 0000312345678901  (PAN Field)
------------------
= 041205DCBA9876FE  (8-Byte Encrypted PIN Block Input)
```

---

## 4. EMV & ISO 8583 Integration: Field 55 (ICC System Related Data)

**Field 55** is the vital bridge connecting the smart card realm with the financial ISO 8583 network. It stores concatenated BER-TLV tags exchanged between card and host.

### 4.1 Field 55 Request TLV Container
Sent in ISO 8583 `0200` Financial Request:

```
Field 55 = Tag 9F26 (08B - ARQC)
        || Tag 9F27 (01B - CID 80)
        || Tag 9F10 (07B - IAD)
        || Tag 9F37 (04B - UN)
        || Tag 9F36 (02B - ATC)
        || Tag 95   (05B - TVR)
        || Tag 9A   (03B - Date)
        || Tag 9C   (01B - Type)
        || Tag 9F02 (06B - Amount)
        || Tag 5F2A (02B - Currency)
        || Tag 82   (02B - AIP)
        || Tag 9F1A (02B - Country)
```

### 4.2 Field 55 Response TLV Container
Returned in ISO 8583 `0210` Financial Response:

```
Field 55 = Tag 91 (0A/10B - Issuer Auth Data / Host ARPC)
        || Tag 8A (02B - Authorization Response Code "00")
```

---

## 5. End-to-End Payment Gateway Transaction Flow Sequence

### 5.1 Interactive Gateway Sequence Diagram

```mermaid
sequenceDiagram
    autonumber
    actor Customer
    participant POS as POS Terminal
    participant Card as EMV Smart Card
    participant Acquirer as Acquirer Gateway
    participant Host as Issuer Bank Host

    Customer->>POS: Tap / Insert Card ($250.00 TRY)
    POS->>Card: SELECT PPSE / AID (A0000000031010)
    Card-->>POS: FCI Template (6F) + AID Name
    POS->>Card: GPO (Get Processing Options)
    Card-->>POS: AIP (82: 7C00) + AFL (94: 08010100)
    POS->>Card: READ RECORD (SFI 1, Record 1)
    Card-->>POS: PAN (5A), Expiry (5F24), Track 2 (57)
    Note over POS: Terminal Risk Mgmt & TVR Evaluation (Tag 95: 0000008000)
    POS->>Card: 1st GENERATE AC (ARQC Request 0x80)
    Card-->>POS: CID (80) + ATC (9F36: 00A4) + ARQC (9F26: A1B2C3D4E5F67890)
    Note over POS: Construct Field 55 TLV Payload
    POS->>Acquirer: Pack ISO 8583 0200 Request (PAN, Amount, Field 55)
    Acquirer->>Host: Route 0200 Request
    Note over Host: Unpack 0200, Validate ARQC, Check Balance, Generate Auth Code 681042
    Host-->>Acquirer: Pack ISO 8583 0210 Response (Field 38: 681042, Field 39: 00, Field 55: Tag 91 ARPC + Tag 8A 00)
    Acquirer-->>POS: Deliver 0210 Response
    POS->>Card: 2nd GENERATE AC (Send Tag 91 ARPC & Tag 8A 00)
    Card-->>POS: CID (40 - TC Transaction Certificate Approved!)
    POS-->>Customer: Display Transaction Approved (Auth Code: 681042)
```

---

### 5.2 Byte-by-Byte Raw Hex Stream Breakdown

#### 1. Packed ISO 8583 `0200` Financial Request Hex Stream:
```
6000000000 0200 F238848108E00000 165412751234567890 000000 000000025000 0926142010 000456 142010 0926 2812 5812 051 001 00 335412751234567890D2812201000000000F POS85831 MCH858300000001 RESTORAN ANKARA TR          ANKARA    TR 071 9F2608A1B2C3D4E5F678909F2701809F100706010A03A000009F3704123456789F360200A4950500000080009A032609259C01009F02060000000250005F2A02079282027C009F1A020792
```

- `6000000000`: 5-byte TPDU Header
- `0200`: 4-character ASCII MTI (Financial Request)
- `F238848108E00000`: Primary Bitmap Hex (Fields 2, 3, 4, 7, 11, 12, 13, 14, 18, 22, 23, 25, 35, 41, 42, 43, 55 active)
- `165412751234567890`: Field 2 (PAN, LLVAR len 16)
- `000000`: Field 3 (Processing Code - Purchase)
- `000000025000`: Field 4 (Amount - 250.00 TRY)
- `071`: Field 55 length (71 bytes binary EMV TLV data)

#### 2. Packed ISO 8583 `0210` Financial Response Hex Stream:
```
6000000000 0210 F238800108E00000 165412751234567890 000000 000000025000 0926142010 000456 123456 00 016 910A3A4B5C6D7E8F900030308A023030
```

- `0210`: MTI Financial Response
- `Field 38`: `123456` (Auth Code)
- `Field 39`: `00` (Approved)
- `Field 55`: `910A3A4B5C6D7E8F900030308A023030` (Tag 91 ARPC + Tag 8A ARC "00")

---

## 6. Application Architecture & Java Package Map

The application follows clean architectural principles written in 100% Java:

```
com.myapp.bankingexample
 ├── MainActivity.java                  # Main Tabbed AppCompat Activity
 ├── emv
 │    ├── EmvTag.java                   # Tag metadata model
 │    ├── EmvTagDictionary.java         # 60+ Tag specification dictionary
 │    ├── TlvElement.java               # Parsed TLV node model
 │    ├── TlvParser.java                # Pure Java BER-TLV parser & TVR/AIP bitmask interpreter
 │    ├── ApduCommand.java              # C-APDU data model
 │    ├── ApduResponse.java             # R-APDU model & Status Word dictionary
 │    ├── EmvApduFactory.java           # APDU command factory
 │    ├── EmvCryptoUtils.java           # ARQC vector builder & Session Key derivation simulator
 │    └── EmvTransactionEngine.java     # 10-Step EMV lifecycle card simulator
 ├── iso8583
 │    ├── IsoFieldSpec.java             # Field specification model (LLVAR/LLLVAR, types)
 │    ├── IsoFieldDictionary.java       # Fields 1-128 specification catalog
 │    ├── IsoFieldValue.java            # Field-value pair
 │    ├── IsoBitmap.java                # Primary/Secondary bitmap bitmask manager
 │    ├── IsoMtiDecoder.java            # MTI 4-digit analyzer
 │    ├── IsoMessage.java               # ISO 8583 message model
 │    ├── IsoMessagePacker.java         # Byte stream serializer
 │    ├── IsoMessageUnpacker.java       # Byte stream deserializer
 │    └── IsoSampleMessages.java        # Pre-packaged 0100, 0200, 0210, 0420, 0800 messages
 ├── simulator
 │    └── EndToEndPaymentSimulator.java # POS <-> Card <-> Gateway <-> Host orchestration simulator
 ├── utils
 │    ├── HexUtils.java                 # Byte/Hex/ASCII conversion & hex dump generator
 │    └── PinBlockCalculator.java       # ISO 9564-1 Format 0/1 PIN block XOR calculator
 └── ui
      ├── MainPagerAdapter.java         # ViewPager2 Fragment State Adapter
      ├── EmvTlvFragment.java           # TLV Parser, PIN Block & ARQC Tool Fragment
      ├── EmvApduFragment.java          # APDU Command Inspector Fragment
      ├── Iso8583Fragment.java          # ISO Message Packer/Unpacker Fragment
      ├── PaymentSimulatorFragment.java # End-to-End Payment Gateway Flow Fragment
      └── KnowledgeBaseFragment.java    # Educational Textbook & Cheat Sheets Fragment
```

---

## 7. Unit Testing & Verification Strategy

The application features comprehensive JUnit 4 unit tests validating all core protocol engines:

### Executed Unit Test Suites:
1. **`TlvParserTest.java`**:
   - `testParseSimpleTlv()`: Validates parsing Tag 9F02 & Tag 5F2A.
   - `testParseConstructedFciTlv()`: Validates parsing nested Tag 6F FCI templates.
   - `testTvrExplanation()`: Validates 5-byte TVR bitmask breakdown strings.
2. **`Iso8583Test.java`**:
   - `testBitmapGeneration()`: Validates primary bitmap bit toggling.
   - `testSecondaryBitmapTrigger()`: Validates Field 70 triggering Secondary Bitmap (Field 1).
   - `testPackAndUnpack0200Message()`: Validates round-trip serialization and Field 55 TLV preservation.
   - `testMtiDecoder()`: Validates MTI `0210` decoding into Financial Response.
3. **`PaymentSimulatorTest.java`**:
   - `testFullEndToEndPaymentSimulation()`: Validates full gateway transaction execution.
4. **`PinBlockCalculatorTest.java`**:
   - `testIso0PinBlockCalculation()`: Validates ISO 9564-1 Format 0 clear PIN field, PAN field extraction, and XOR calculation.
   - `testIso1PinBlockCalculation()`: Validates Format 1 PIN block formatting.
5. **`EmvCryptoTest.java`**:
   - `testArqcVectorCalculation()`: Validates Session Key derivation and EMV Book 2 Table 13 34-byte vector construction.

### Test Execution Command:
```bash
./gradlew test
```
**Result**: `12 passed, 0 skipped, 0 failed` (100% Pass Rate).

---

## 8. Build & Installation Guide

### Prerequisites:
- JDK 11 or higher
- Android SDK (API Level 34 / 36)
- Gradle 8.x or AGP 9.x

### Build Commands:

1. **Assemble Debug APK**:
   ```bash
   ./gradlew app:assembleDebug
   ```

2. **Run All Unit Tests**:
   ```bash
   ./gradlew test
   ```

3. **Deploy to Device / Emulator**:
   ```bash
   ./gradlew app:installDebug
   ```

---

### License & Author
Developed as an open reference guide for payment systems education and software engineering excellence.
