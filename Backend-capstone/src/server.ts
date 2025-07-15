import express, { Request, Response, NextFunction } from "express";
import dotenv from "dotenv";
import os from "os";
import authRoutes from "./routes/auth.routes";
import articleRoutes from "./routes/article.routes";
import speechRoutes from "./models/speech.routes";
import historyRoutes from "./routes/history.routes";
import path from "path";

dotenv.config();

const app = express();
app.use(express.json());

app.use("/api/users", authRoutes);
app.use("/api/articles", articleRoutes);
app.use("/api/models", speechRoutes);
app.use("/api/history", historyRoutes);
app.use("/uploads", express.static(path.join(__dirname, "../public/uploads")));

app.use((err: any, req: Request, res: Response, next: NextFunction) => {
    res.status(err.status ? Number(err.status) : 500).json({
        success: false,
        message: err.message || "Internal Server Error",
    });
});

function getLocalIP(): string {
    const interfaces = os.networkInterfaces();
    for (const name in interfaces) {
        if (!interfaces[name]) continue;
        for (const net of interfaces[name]!) {
            if (net.family === "IPv4" && !net.internal) {
                return net.address;
            }
        }
    }
    return "localhost";
}

const PORT = process.env.PORT ? parseInt(process.env.PORT, 10) : 3000;
const ip = getLocalIP();

app.listen(PORT, "0.0.0.0", () => {
    console.log(`Server berjalan di http://${ip}:${PORT}`);
});
