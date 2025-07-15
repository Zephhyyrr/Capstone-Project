import { PrismaClient } from "@prisma/client";
import { RequestHandler } from "express";
import { authenticateToken } from "../middleware/auth";
import { ResponseError } from "../error/response.error";
import { upload } from "../middleware/upload";
import * as fs from "fs/promises";
import * as path from "path";
import os from "os";

const prisma = new PrismaClient();
const uploadsFolder = path.join(__dirname, "../../public/uploads");

function getLocalIp(): string {
    const interfaces = os.networkInterfaces();

    for (const name of Object.keys(interfaces)) {
        // Cek hanya interface Wi-Fi atau Ethernet
        if (!["Wi-Fi", "Ethernet", "wlan0", "eth0"].includes(name)) continue;

        for (const iface of interfaces[name]!) {
            if (iface.family === "IPv4" && !iface.internal) {
                return iface.address;
            }
        }
    }

    return "localhost";
}

const baseImageUrl = `http://${getLocalIp()}:3000/uploads`;

function getImageUrl(filename: string | null) {
    if (!filename) return null;
    return `${baseImageUrl}/${filename}`;
}

// Create Article
export const create: RequestHandler[] = [
    authenticateToken,
    upload.single("image"),
    async (req, res, next) => {
        try {
            const { title, content, urlArticle } = req.body;

            if (!title || !content) {
                throw new ResponseError(400, "Title and content are required");
            }

            if (!req.file) {
                throw new ResponseError(400, "Image file is required");
            }

            const newFilename = path.basename(req.file.filename); // gunakan filename dari middleware

            const article = await prisma.article.create({
                data: {
                    title,
                    content,
                    image: newFilename,
                    urlArticle,
                },
            });

            res.status(201).json({
                success: true,
                message: "Article successfully created",
                data: {
                    ...article,
                    imageUrl: getImageUrl(article.image),
                },
            });
        } catch (error) {
            next(error);
        }
    },
];

// Update Article
export const update: RequestHandler[] = [
    authenticateToken,
    upload.single("image"),
    async (req, res, next) => {
        try {
            const id = Number(req.params.id);
            if (isNaN(id)) throw new ResponseError(400, "Invalid article ID");

            const { title, content, urlArticle } = req.body;

            if (!title || !content) {
                throw new ResponseError(400, "Title and content are required");
            }

            const oldArticle = await prisma.article.findUnique({ where: { id } });
            if (!oldArticle) {
                throw new ResponseError(404, "Article not found");
            }

            let newFilename = oldArticle.image;

            if (req.file) {
                newFilename = path.basename(req.file.filename);

                // Hapus file lama jika ada
                if (oldArticle.image) {
                    const oldImagePath = path.join(uploadsFolder, oldArticle.image);
                    try {
                        await fs.unlink(oldImagePath);
                    } catch {
                        // file mungkin sudah dihapus, abaikan error
                    }
                }
            }

            const updatedArticle = await prisma.article.update({
                where: { id },
                data: {
                    title,
                    content,
                    urlArticle,
                    image: newFilename,
                },
            });

            res.status(200).json({
                success: true,
                message: "Article successfully updated",
                data: {
                    ...updatedArticle,
                    imageUrl: getImageUrl(updatedArticle.image),
                },
            });
        } catch (error) {
            next(error);
        }
    },
];


// Get All Articles
export const get: RequestHandler[] = [
    authenticateToken,
    async (req, res, next) => {
        try {
            const articles = await prisma.article.findMany();

            const articlesWithImageUrl = articles.map((a) => ({
                ...a,
                imageUrl: getImageUrl(a.image),
            }));

            res.status(200).json({
                success: true,
                message: "Articles retrieved successfully",
                data: articlesWithImageUrl,
            });
        } catch (error) {
            next(error);
        }
    },
];

// Get Article by ID
export const getById: RequestHandler[] = [
    authenticateToken,
    async (req, res, next) => {
        try {
            const id = Number(req.params.id);
            if (isNaN(id)) throw new ResponseError(400, "Invalid article ID");

            const article = await prisma.article.findUnique({ where: { id } });

            if (!article) {
                res.status(404).json({ error: "Article not found" });
                return;
            }

            res.status(200).json({
                success: true,
                message: "Article retrieved successfully",
                data: {
                    ...article,
                    imageUrl: getImageUrl(article.image),
                },
            });
        } catch (error) {
            next(error);
        }
    },
];

// Delete Article
export const deleteArticle: RequestHandler[] = [
    authenticateToken,
    async (req, res, next) => {
        try {
            const id = Number(req.params.id);
            if (isNaN(id)) throw new ResponseError(400, "Invalid article ID");

            const articleToDelete = await prisma.article.findUnique({ where: { id } });

            if (!articleToDelete) {
                throw new ResponseError(404, "Article not found");
            }

            if (articleToDelete.image) {
                const imagePath = path.join(uploadsFolder, articleToDelete.image);
                try {
                    await fs.unlink(imagePath);
                } catch {
                    // file mungkin sudah tidak ada, abaikan error
                }
            }

            await prisma.article.delete({ where: { id } });

            // 204 No Content tidak boleh ada body, jadi gunaxkan 200 dengan pesan sukses
            res.status(200).json({
                success: true,
                message: "Article successfully deleted",
            });
        } catch (error) {
            next(error);
        }
    },
];
