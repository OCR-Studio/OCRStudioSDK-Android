/*
  Copyright (c) 2024-2026, OCR Studio
  All rights reserved.
*/

package ai.ocrstudio.sdk.sample;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class LivenessInstruction {
    @NonNull
    public String text;
    @Nullable
    public Integer mask;

    public LivenessInstruction(@NonNull String text, @Nullable Integer mask){
        this.text = text;
        this.mask = mask;
    }
}
