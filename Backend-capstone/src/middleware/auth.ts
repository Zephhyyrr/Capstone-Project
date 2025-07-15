import { Request, Response, NextFunction } from "express";
import jwt from "jsonwebtoken";
import dotenv from "dotenv";
import { PrismaClient } from "@prisma/client";

dotenv.config();

const JWT_REFRESH_SECRET = process.env.JWT_REFRESH_SECRET;
const prisma = new PrismaClient();

if (!JWT_REFRESH_SECRET) {
    throw new Error("JWT_REFRESH_SECRET must be defined in environment variables");
}

export const authenticateToken = async (req: Request, res: Response, next: NextFunction): Promise<void> => {
    const authHeader = req.headers.authorization;
    if (!authHeader?.startsWith("Bearer ")) {
        res.status(401).json({ error: "Access denied, token missing or malformed!" });
        return;
    }

    const token = authHeader.split(" ")[1];

    try {
        // Verifikasi token dengan JWT
        const decoded = jwt.verify(token, JWT_REFRESH_SECRET) as { userId: number };
        
        // Cek keberadaan token di database
        const refreshToken = await prisma.refreshToken.findFirst({
            where: {
                token: token,
                userId: decoded.userId
            }
        });

        // Jika token tidak ditemukan di database
        if (!refreshToken) {
            res.status(403).json({ error: "Invalid Token!" });
            return;
        }

        // Cek waktu pembuatan token, jika lebih dari 1 jam maka token tidak valid
        const tokenCreationTime = new Date(refreshToken.createdAt).getTime();
        const currentTime = new Date().getTime();
        const oneHourInMillis = 60 * 60 * 1000; // 1 jam dalam milidetik
        
        if (currentTime - tokenCreationTime > oneHourInMillis) {
            // Hapus token yang sudah kadaluarsa
            await prisma.refreshToken.delete({
                where: {
                    id: refreshToken.id
                }
            });
            res.status(403).json({ error: "Token is Expired" });
            return;
        }

        // Set user pada request
        req.user = { userId: decoded.userId };
        next();
    } catch (error) {
        res.status(403).json({ error: "Invalid or expired token!" });
    }
};

// Fungsi untuk membuat refresh token baru
export const createRefreshToken = async (userId: number): Promise<string> => {
    // Hapus semua refresh token yang ada untuk user ini
    await prisma.refreshToken.deleteMany({
        where: {
            userId: userId
        }
    });
    
    // Buat token baru
    const refreshToken = jwt.sign(
        { userId }, 
        JWT_REFRESH_SECRET, 
        { expiresIn: '1h' }
    );
    
    // Simpan token baru ke database
    await prisma.refreshToken.create({
        data: {
            token: refreshToken,
            userId: userId
        }
    });
    
    return refreshToken;
};

export const cleanupExpiredTokens = async (): Promise<void> => {
    const oneHourAgo = new Date(Date.now() - 60 * 60 * 1000);
    
    await prisma.refreshToken.deleteMany({
        where: {
            createdAt: {
                lt: oneHourAgo
            }
        }
    });
};