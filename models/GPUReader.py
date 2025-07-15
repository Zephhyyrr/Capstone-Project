import torch
print(torch.cuda.is_available())  # Harus menghasilkan True
print(torch.cuda.get_device_name(0))  # Harus menunjukkan RTX 3050