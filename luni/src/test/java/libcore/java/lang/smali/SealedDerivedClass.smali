.class Llibcore/java/lang/sealedclasses/SealedDerivedClass;
.super Ljava/lang/Object;
.source "SealedDerivedClass.java"


# annotations
.annotation runtime Ldalvik/annotation/PermittedSubclasses;
    classes = {
        Llibcore/java/lang/sealedclasses/OpenDerivedClass;
    }
.end annotation


# direct methods
.method constructor <init>()V
    .registers 1

    .line 6
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method
