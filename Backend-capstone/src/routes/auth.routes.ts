import { Router } from "express";
import { login, register, updateUser, logout, getCurrentUserController, deleteUser, updateProfilePhoto } from "../controllers/user.controller";
import { getAllUsers, fetchUserById} from "../services/user.service";
import { authenticateToken } from "../middleware/auth";
import { upload } from "../middleware/upload";

const router = Router();

router.post("/register", register);

router.post("/login", login);

router.get("/getUsers", getAllUsers);

router.get("/getUser/", fetchUserById);  

router.get("/current", authenticateToken, getCurrentUserController);

router.post("/uploadPhotos", authenticateToken, upload.single("profileImage"), updateProfilePhoto);

router.put("/update", authenticateToken, updateUser)

router.post("/logout", authenticateToken, logout)

router.delete("/delete", authenticateToken, deleteUser)

export default router;
