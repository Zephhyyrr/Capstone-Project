import { PrismaClient } from '@prisma/client';
import { ResponseError } from '../error/response.error';

const prisma = new PrismaClient();

export const getAllHistory = async (userId: number) => {
    try {
        await prisma.$connect();

        const userExists = await prisma.user.findUnique({
            where: { id: userId }
        });

        if (!userExists) {
            throw new ResponseError(404, "User not found");
        }

        const history = await prisma.history.findMany({
            where: { userId },
            orderBy: { createdAt: 'desc' }
        });

        // Transform fileAudio to include /uploads/ path
        const transformedHistory = history.map(item => ({
            ...item,
            fileAudio: `/uploads/${item.fileAudio}`
        }));

        return transformedHistory;
    } catch (error) {
        if (error instanceof ResponseError) {
            throw error;
        }

        if ((error as any)?.name === 'PrismaClientKnownRequestError') {
            throw new ResponseError(500, `Database error: ${(error as any)?.message || 'Unknown error'}`);
        }

        throw new ResponseError(500, "Failed to fetch history");
    }
};

export const getHistoryById = async (userId: number, historyId: number) => {
    try {
        const history = await prisma.history.findUnique({
            where: { id: historyId, userId },
        });

        if (!history) {
            throw new ResponseError(404, "History not found");
        }

        // Transform fileAudio to include /uploads/ path
        const transformedHistory = {
            ...history,
            fileAudio: `/uploads/${history.fileAudio}`
        };

        return transformedHistory;
    } catch (error) {
        if (error instanceof ResponseError) {
            throw error;
        }
        throw new ResponseError(500, "Failed to fetch history");
    }
};

export const createHistory = async (
    userId: number,
    fileAudio: string,
    originalParagraph: string,
    correctedParagraph: string = "",
    grammarAnalysis?: any
) => {
    try {
        const history = await prisma.history.create({
            data: {
                userId,
                fileAudio,
                originalParagraph,
                correctedParagraph,
                grammarAnalysis,
            }
        });

        return history;
    } catch (error) {
        throw new ResponseError(500, "Failed to create history");
    }
};

// Fungsi baru untuk update history
export const updateHistory = async (
    userId: number,
    fileAudio: string,
    correctedParagraph: string,
    grammarAnalysis?: any
) => {
    try {
        // Cari history berdasarkan userId dan fileAudio
        const existingHistory = await prisma.history.findFirst({
            where: {
                userId,
                fileAudio,
            },
            orderBy: {
                createdAt: 'desc' // Ambil yang terbaru jika ada duplikat
            }
        });

        if (!existingHistory) {
            throw new ResponseError(404, "History not found for update");
        }

        // Update history dengan corrected paragraph dan grammar analysis
        const updatedHistory = await prisma.history.update({
            where: {
                id: existingHistory.id
            },
            data: {
                correctedParagraph,
                grammarAnalysis,
                updatedAt: new Date()
            }
        });

        return updatedHistory;
    } catch (error) {
        if (error instanceof ResponseError) {
            throw error;
        }
        throw new ResponseError(500, "Failed to update history");
    }
};

export const deleteHistory = async (userId: number, historyId: number) => {
    try {
        const history = await prisma.history.findUnique({
            where: { id: historyId, userId },
        });

        if (!history) {
            throw new ResponseError(404, "History not found");
        }

        await prisma.history.delete({
            where: { id: historyId, userId }
        });

        return { message: "History deleted successfully" };
    } catch (error) {
        if (error instanceof ResponseError) {
            throw error;
        }
        throw new ResponseError(500, "Failed to delete history");
    }
};