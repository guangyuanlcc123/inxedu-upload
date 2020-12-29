package com.inxedu.wxos.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

/**
 * @author Dongguabai
 * @Description
 * @Date 创建于 2020-01-26 23:49
 */
public class DownLoadProcessor {

    /**
     * 文件来源路径
     */
    private String source;

    /**
     * 目标路径
     */
    private String target;

    /**
     * 每个线程读取字节长度
     */
    private Long eachSize;

    /**
     * 读取文件总大小
     */
    private Long totalLength;

    /**
     * 源文件
     */
    private File sourceFile;

    /**
     * 目标文件
     */
    private File targetFile;

    /**
     * 并行数量
     */
    private int parallelism = 3;

    public DownLoadProcessor(String source, String target) {
        this.source = source;
        this.target = target;
    }

    public void start() throws IOException {
        sourceFile = new File(source);
        targetFile = new File(target);
        totalLength = sourceFile.length();
        RandomAccessFile raf = new RandomAccessFile(targetFile, "rw");
        //raf.setLength(totalLength);
        raf.close();

        eachSize = totalLength / parallelism;
        CompletableFuture[] completableFutures = IntStream.range(0, parallelism).boxed().map(i -> CompletableFuture
                .runAsync(() -> download(i))
                .whenComplete((result, e) -> System.out.println(i + "-> over..."))).toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(completableFutures).join();
    }

    private void download(Integer index) {
        System.out.println(index);

        try (FileInputStream is = new FileInputStream(sourceFile);
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             RandomAccessFile accessFile = new RandomAccessFile(targetFile, "rw")) {

            //每次读取1024
            byte[] buff = new byte[1024];
            //todo 待优化

            //获取当前线程读取区间,最后一个读取剩余部分
            int start = (int) (index * eachSize);
            int end = (int) (index == parallelism - 1 ? totalLength - 1 : (index + 1) * eachSize - 1);
            int length = end - start + 1;
            int readLength = 0;
            is.skip(start);
            int len;
            //下载文件并写入本地
            while ((len = is.read(buff)) != -1 && readLength <= length) {
                baos.write(buff, 0, len);
                readLength += len;
            }
            byte[] readData = baos.toByteArray();
            byte[] result = baos.size() > length ? Arrays.copyOf(readData, length) : readData;
            System.out.println(result.length);
            accessFile.seek(start);
            accessFile.write(result);
            System.out.println(start + "-" + end + " over.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
