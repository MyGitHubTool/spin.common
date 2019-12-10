/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spin.core.util;

import org.spin.core.security.MessageDigestAlgorithms;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 简化摘要算法{@link MessageDigest} 操作。
 * 这个Class是immutable且线程安全的。
 * 默认的字符串返回结果，均为大写形式。
 *
 * @version 1.0
 */
public abstract class DigestUtils {
    private static final int STREAM_BUFFER_LENGTH = 1024;

    private DigestUtils() {
    }

    /**
     * Read through an InputStream and returns the digest for the data
     *
     * @param digest The MessageDigest to use (e.g. MD5)
     * @param data   Data to digest
     * @return the digest
     * @throws IOException On error reading from the stream
     */
    private static byte[] digest(final MessageDigest digest, final InputStream data) throws IOException {
        return updateDigest(digest, data).digest();
    }

    /**
     * Returns a <code>MessageDigest</code> for the given <code>algorithm</code>.
     *
     * @param algorithm the name of the algorithm requested. See <a
     *                  href="http://docs.oracle.com/javase/6/docs/technotes/guides/security/crypto/CryptoSpec.html#AppA"
     *                  >Appendix A in the Java Cryptography Architecture Reference Guide</a> for information about standard
     *                  algorithm names.
     * @return A digest instance.
     * @throws IllegalArgumentException when a {@link NoSuchAlgorithmException} is caught.
     * @see MessageDigest#getInstance(String)
     */
    public static MessageDigest getDigest(final MessageDigestAlgorithms algorithm) {
        try {
            return MessageDigest.getInstance(algorithm.value());
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Returns an MD2 MessageDigest.
     *
     * @return An MD2 digest instance.
     * @throws IllegalArgumentException when a {@link NoSuchAlgorithmException} is caught, which should never happen because MD2 is a
     *                                  built-in algorithm
     * @see MessageDigestAlgorithms#MD2
     * @since 1.7
     */
    public static MessageDigest getMd2Digest() {
        return getDigest(MessageDigestAlgorithms.MD2);
    }

    /**
     * Returns an MD5 MessageDigest.
     *
     * @return An MD5 digest instance.
     * @throws IllegalArgumentException when a {@link NoSuchAlgorithmException} is caught, which should never happen because MD5 is a
     *                                  built-in algorithm
     * @see MessageDigestAlgorithms#MD5
     */
    public static MessageDigest getMd5Digest() {
        return getDigest(MessageDigestAlgorithms.MD5);
    }

    /**
     * Returns an SHA-1 digest.
     *
     * @return An SHA-1 digest instance.
     * @throws IllegalArgumentException when a {@link NoSuchAlgorithmException} is caught, which should never happen because SHA-1 is a
     *                                  built-in algorithm
     * @see MessageDigestAlgorithms#SHA_1
     * @since 1.7
     */
    public static MessageDigest getSha1Digest() {
        return getDigest(MessageDigestAlgorithms.SHA_1);
    }

    /**
     * Returns an SHA-256 digest.
     * <p>
     * Throws a <code>RuntimeException</code> on JRE versions prior to 1.4.0.
     * </p>
     *
     * @return An SHA-256 digest instance.
     * @throws IllegalArgumentException when a {@link NoSuchAlgorithmException} is caught, which should never happen because SHA-256 is a
     *                                  built-in algorithm
     * @see MessageDigestAlgorithms#SHA_256
     */
    public static MessageDigest getSha256Digest() {
        return getDigest(MessageDigestAlgorithms.SHA_256);
    }

    /**
     * Returns an SHA-384 digest.
     * <p>
     * Throws a <code>RuntimeException</code> on JRE versions prior to 1.4.0.
     * </p>
     *
     * @return An SHA-384 digest instance.
     * @throws IllegalArgumentException when a {@link NoSuchAlgorithmException} is caught, which should never happen because SHA-384 is a
     *                                  built-in algorithm
     * @see MessageDigestAlgorithms#SHA_384
     */
    public static MessageDigest getSha384Digest() {
        return getDigest(MessageDigestAlgorithms.SHA_384);
    }

    /**
     * Returns an SHA-512 digest.
     * <p>
     * Throws a <code>RuntimeException</code> on JRE versions prior to 1.4.0.
     * </p>
     *
     * @return An SHA-512 digest instance.
     * @throws IllegalArgumentException when a {@link NoSuchAlgorithmException} is caught, which should never happen because SHA-512 is a
     *                                  built-in algorithm
     * @see MessageDigestAlgorithms#SHA_512
     */
    public static MessageDigest getSha512Digest() {
        return getDigest(MessageDigestAlgorithms.SHA_512);
    }

    /**
     * Calculates the MD2 digest and returns the value as a 16 element <code>byte[]</code>.
     *
     * @param data Data to digest
     * @return MD2 digest
     * @since 1.7
     */
    public static byte[] md2(final byte[] data) {
        return getMd2Digest().digest(data);
    }

    /**
     * Calculates the MD2 digest and returns the value as a 16 element <code>byte[]</code>.
     *
     * @param data Data to digest
     * @return MD2 digest
     * @throws IOException On error reading from the stream
     * @since 1.7
     */
    public static byte[] md2(final InputStream data) throws IOException {
        return digest(getMd2Digest(), data);
    }

    /**
     * Calculates the MD2 digest and returns the value as a 16 element <code>byte[]</code>.
     *
     * @param data Data to digest; converted to bytes using {@link StringUtils#getBytesUtf8(String)}
     * @return MD2 digest
     * @since 1.7
     */
    public static byte[] md2(final String data) {
        return md2(StringUtils.getBytesUtf8(data));
    }

    /**
     * Calculates the MD2 digest and returns the value as a 32 character hex string.
     *
     * @param data Data to digest
     * @return MD2 digest as a hex string
     * @since 1.7
     */
    public static String md2Hex(final byte[] data) {
        return HexUtils.encodeHexStringL(md2(data));
    }

    /**
     * Calculates the MD2 digest and returns the value as a 32 character hex string.
     *
     * @param data Data to digest
     * @return MD2 digest as a hex string
     * @throws IOException On error reading from the stream
     * @since 1.7
     */
    public static String md2Hex(final InputStream data) throws IOException {
        return HexUtils.encodeHexStringL(md2(data));
    }

    /**
     * Calculates the MD2 digest and returns the value as a 32 character hex string.
     *
     * @param data Data to digest
     * @return MD2 digest as a hex string
     * @since 1.7
     */
    public static String md2Hex(final String data) {
        return HexUtils.encodeHexStringL(md2(data));
    }

    /**
     * Calculates the MD5 digest and returns the value as a 16 element <code>byte[]</code>.
     *
     * @param data Data to digest
     * @return MD5 digest
     */
    public static byte[] md5(final byte[] data) {
        return getMd5Digest().digest(data);
    }

    /**
     * Calculates the MD5 digest and returns the value as a 16 element <code>byte[]</code>.
     *
     * @param data Data to digest
     * @return MD5 digest
     * @throws IOException On error reading from the stream
     * @since 1.4
     */
    public static byte[] md5(final InputStream data) throws IOException {
        return digest(getMd5Digest(), data);
    }

    /**
     * Calculates the MD5 digest and returns the value as a 16 element <code>byte[]</code>.
     *
     * @param data Data to digest; converted to bytes using {@link StringUtils#getBytesUtf8(String)}
     * @return MD5 digest
     */
    public static byte[] md5(final String data) {
        return md5(StringUtils.getBytesUtf8(data));
    }

    /**
     * Calculates the MD5 digest and returns the value as a 32 character hex string.
     *
     * @param data Data to digest
     * @return MD5 digest as a hex string
     */
    public static String md5Hex(final byte[] data) {
        return HexUtils.encodeHexStringU(md5(data));
    }

    /**
     * Calculates the MD5 digest and returns the value as a 32 character hex string.
     *
     * @param data Data to digest
     * @return MD5 digest as a hex string
     * @throws IOException On error reading from the stream
     * @since 1.4
     */
    public static String md5Hex(final InputStream data) throws IOException {
        return HexUtils.encodeHexStringU(md5(data));
    }

    /**
     * 计算字符串的MD5值并以32位大写Hex的形式返回
     *
     * @param data 待计算字符串
     * @return 32位大写MD5
     */
    public static String md5Hex(final String data) {
        return HexUtils.encodeHexStringU(md5(data));
    }

    /**
     * 计算字符串的MD5值并以32位小写Hex的形式返回
     *
     * @param data 待计算字符串
     * @return 32位小写MD5
     */
    public static String md5HexL(final String data) {
        return HexUtils.encodeHexStringL(md5(data));
    }

    /**
     * 计算字符串的MD5值并以16位大写Hex的形式返回
     *
     * @param data 待计算字符串
     * @return 16位大写MD5
     */
    public static String md5Hex16(final String data) {
        return HexUtils.encodeHexStringU(md5(data)).substring(8, 24);
    }

    /**
     * 计算字符串的MD5值并以16位小写Hex的形式返回
     *
     * @param data 待计算字符串
     * @return 16位小写MD5
     */
    public static String md5HexL16(final String data) {
        return HexUtils.encodeHexStringL(md5(data)).substring(8, 24);
    }

    /**
     * Calculates the SHA-1 digest and returns the value as a <code>byte[]</code>.
     *
     * @param data Data to digest
     * @return SHA-1 digest
     * @since 1.7
     */
    public static byte[] sha1(final byte[] data) {
        return getSha1Digest().digest(data);
    }

    /**
     * Calculates the SHA-1 digest and returns the value as a <code>byte[]</code>.
     *
     * @param data Data to digest
     * @return SHA-1 digest
     * @throws IOException On error reading from the stream
     * @since 1.7
     */
    public static byte[] sha1(final InputStream data) throws IOException {
        return digest(getSha1Digest(), data);
    }

    /**
     * Calculates the SHA-1 digest and returns the value as a <code>byte[]</code>.
     *
     * @param data Data to digest; converted to bytes using {@link StringUtils#getBytesUtf8(String)}
     * @return SHA-1 digest
     */
    public static byte[] sha1(final String data) {
        return sha1(StringUtils.getBytesUtf8(data));
    }

    /**
     * Calculates the SHA-1 digest and returns the value as a hex string.
     *
     * @param data Data to digest
     * @return SHA-1 digest as a hex string
     * @since 1.7
     */
    public static String sha1Hex(final byte[] data) {
        return HexUtils.encodeHexStringU(sha1(data));
    }

    /**
     * Calculates the SHA-1 digest and returns the value as a hex string.
     *
     * @param data Data to digest
     * @return SHA-1 digest as a hex string
     * @since 1.7
     */
    public static String sha1HexL(final byte[] data) {
        return HexUtils.encodeHexStringL(sha1(data));
    }

    /**
     * Calculates the SHA-1 digest and returns the value as a hex string.
     *
     * @param data Data to digest
     * @return SHA-1 digest as a hex string
     * @throws IOException On error reading from the stream
     * @since 1.7
     */
    public static String sha1Hex(final InputStream data) throws IOException {
        return HexUtils.encodeHexStringU(sha1(data));
    }

    /**
     * Calculates the SHA-1 digest and returns the value as a hex string.
     *
     * @param data Data to digest
     * @return SHA-1 digest as a hex string
     * @since 1.7
     */
    public static String sha1Hex(final String data) {
        return HexUtils.encodeHexStringU(sha1(data));
    }

    public static byte[] sha256(final byte[] data) {
        return getSha256Digest().digest(data);
    }

    public static byte[] sha256(final InputStream data) throws IOException {
        return digest(getSha256Digest(), data);
    }

    public static byte[] sha256(final String data) {
        return sha256(StringUtils.getBytesUtf8(data));
    }

    public static String sha256Hex(final byte[] data) {
        return HexUtils.encodeHexStringU(sha256(data));
    }

    public static String sha256Hex(final InputStream data) throws IOException {
        return HexUtils.encodeHexStringU(sha256(data));
    }

    public static String sha256Hex(final String data) {
        return HexUtils.encodeHexStringU(sha256(data));
    }

    public static byte[] sha384(final byte[] data) {
        return getSha384Digest().digest(data);
    }

    public static byte[] sha384(final InputStream data) throws IOException {
        return digest(getSha384Digest(), data);
    }

    public static byte[] sha384(final String data) {
        return sha384(StringUtils.getBytesUtf8(data));
    }

    public static String sha384Hex(final byte[] data) {
        return HexUtils.encodeHexStringU(sha384(data));
    }

    public static String sha384Hex(final InputStream data) throws IOException {
        return HexUtils.encodeHexStringU(sha384(data));
    }

    public static String sha384Hex(final String data) {
        return HexUtils.encodeHexStringU(sha384(data));
    }

    public static byte[] sha512(final byte[] data) {
        return getSha512Digest().digest(data);
    }

    public static byte[] sha512(final InputStream data) throws IOException {
        return digest(getSha512Digest(), data);
    }

    public static byte[] sha512(final String data) {
        return sha512(StringUtils.getBytesUtf8(data));
    }

    public static String sha512Hex(final byte[] data) {
        return HexUtils.encodeHexStringU(sha512(data));
    }

    public static String sha512Hex(final InputStream data) throws IOException {
        return HexUtils.encodeHexStringU(sha512(data));
    }

    public static String sha512Hex(final String data) {
        return HexUtils.encodeHexStringU(sha512(data));
    }

    public static MessageDigest updateDigest(final MessageDigest messageDigest, final byte[] valueToDigest) {
        messageDigest.update(valueToDigest);
        return messageDigest;
    }

    public static MessageDigest updateDigest(final MessageDigest digest, final InputStream data) throws IOException {
        final byte[] buffer = new byte[STREAM_BUFFER_LENGTH];
        int read = data.read(buffer, 0, STREAM_BUFFER_LENGTH);

        while (read > -1) {
            digest.update(buffer, 0, read);
            read = data.read(buffer, 0, STREAM_BUFFER_LENGTH);
        }

        return digest;
    }

    public static MessageDigest updateDigest(final MessageDigest messageDigest, final String valueToDigest) {
        messageDigest.update(StringUtils.getBytesUtf8(valueToDigest));
        return messageDigest;
    }
}
