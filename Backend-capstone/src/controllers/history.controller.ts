import { RequestHandler } from 'express';
import { getAllHistory, getHistoryById, deleteHistory } from '../services/history.service';
import { PrismaClient } from '@prisma/client';

const prisma = new PrismaClient();

export const getHistory: RequestHandler = async (req, res, next) => {
    try {
        const userId = (req as any).user?.userId;

        if (!userId) {
            res.status(401).json({
                success: false,
                message: "Unauthorized - User ID not found"
            });
            return;
        }

        const history = await getAllHistory(Number(userId));

        res.status(200).json({
            success: true,
            message: "History retrieved successfully",
            data: history
        });
        return;
    } catch (error) {
        next(error);
    }
};

export const getHistoryByIdController: RequestHandler = async (req, res, next) => {
    try {
        const userId = (req as any).user?.userId;
        const historyId = Number(req.params.id);

        if (!userId) {
            res.status(401).json({
                success: false,
                message: "Unauthorized - User ID not found"
            });
            return;
        }

        if (isNaN(historyId)) {
            res.status(400).json({
                success: false,
                message: "Invalid history ID"
            });
            return;
        }

        const history = await getHistoryById(Number(userId), historyId);

        res.status(200).json({
            success: true,
            message: "History retrieved successfully",
            data: history
        });
        return;
    } catch (error) {
        next(error);
    }
};

export const saveHistory: RequestHandler = async (req, res, next) => {
    try {
        const userId = (req as any).user?.userId;
        const {
            audioFileName,
            originalParagraph,
            correctedParagraph,
            grammarAnalysis
        } = req.body;

        if (!userId) {
            res.status(401).json({
                success: false,
                message: "Unauthorized - User ID not found"
            });
            return;
        }

        if (!audioFileName || !originalParagraph) {
            res.status(400).json({
                success: false,
                message: "audioFileName and originalParagraph are required"
            });
            return;
        }

        const existingHistory = await prisma.history.findFirst({
            where: {
                userId: Number(userId),
                fileAudio: audioFileName
            }
        });

        let savedHistory;

        if (existingHistory) {
            savedHistory = await prisma.history.update({
                where: { id: existingHistory.id },
                data: {
                    correctedParagraph: correctedParagraph || existingHistory.correctedParagraph,
                    grammarAnalysis: grammarAnalysis || existingHistory.grammarAnalysis,
                    updatedAt: new Date()
                }
            });
        } else {
            savedHistory = await prisma.history.create({
                data: {
                    userId: Number(userId),
                    fileAudio: audioFileName,
                    originalParagraph,
                    correctedParagraph: correctedParagraph || "",
                    grammarAnalysis: grammarAnalysis || null
                }
            });
        }

        const transformedHistory = {
            ...savedHistory,
            fileAudio: `/uploads/${savedHistory.fileAudio}`
        };

        res.status(200).json({
            success: true,
            message: existingHistory ? "History updated successfully" : "History saved successfully",
            data: transformedHistory
        });
        return;
    } catch (error) {
        console.error("Error saving history:", error);
        next(error);
    }
};

export const deleteHistoryController: RequestHandler = async (req, res, next) => {
    try {
        const userId = (req as any).user?.userId;
        const historyId = Number(req.params.id);

        if (!userId) {
            res.status(401).json({
                success: false,
                message: "Unauthorized - User ID not found"
            });
            return;
        }

        if (isNaN(historyId)) {
            res.status(400).json({
                success: false,
                message: "Invalid history ID"
            });
            return;
        }

        const result = await deleteHistory(Number(userId), historyId);

        res.status(200).json({
            success: true,
            message: result.message
        });
        return;
    } catch (error) {
        next(error);
    }
};
