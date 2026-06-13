# PlayerBuff

プレイヤーのAttributeを変動させ新しいBuffを作成するBukkitプラグイン。

## コマンド

### /playerbuff
Configを再読み込み。
```
/playerbuff reload
```
権限: `playerbuff.command.main`

### /pbs (エイリアス: /playerbuffset, /pbset)
プレイヤーにバフを付与。
```
/pbs <PlayerName> <BuffType> <level> <duration> [mode]
```
- **PlayerName**: オンラインプレイヤー
- **BuffType**: Health / Damage / Armor / Toughness / Speed
- **level**: 効果量 (Healthなら1HP、Speedなら0.2で移動速度100%Up)
- **duration**: 効果時間（秒）。ログアウトすると効果が剥がれる。
- **mode** (オプション): `add` で残り時間に加算、`replace` で上書き（省略時は `replace`）

権限: `playerbuff.command.set`

## ワールド固有機能

### free_field ワールド
特定のMythicMobs防具一式を装着すると最大HPが上昇します。
- HW2022装備 / HW2023装備の両対応
- config.yml の `worldName` / `Head` / `Chest` / `Legs` / `Boots` / `Head22` / `Chest22` / `Legs22` / `Boots22` で設定

### dream ワールド
- 特定のMythicMobsアイテムをメインハンドに持つと攻撃力が上昇 (`worldName2` / `MainHand` / `damage_Amount`)
- 移動速度低下・最大HP上昇のスカラー効果が常時適用

## 権限

| 権限ノード | 説明 |
|---|---|
| `playerbuff.command.*` | 全コマンド使用可 (デフォルト: op) |
| `playerbuff.command.main` | `/playerbuff` コマンド (デフォルト: op) |
| `playerbuff.command.set` | `/pbs` コマンド (デフォルト: op) |

## ビルド

```bash
./gradlew build
```

出力: `build/libs/PlayerBuff-<version>.jar`

## 依存

- Paper 1.15.2 (または互換API)
- MythicMobs (4.12.0+)
